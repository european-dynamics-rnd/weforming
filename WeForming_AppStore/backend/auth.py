"""Keycloak / OpenID Connect integration.

Identity is owned by an external Keycloak. The frontend keeps posting
username+password to our own ``/api/token`` endpoint, which proxies to Keycloak's
Direct Access Grant (see ``main.py``). Every protected request then carries a
Keycloak-issued RS256 access token, which we validate offline against the realm's
JWKS and inspect for the app's client roles.
"""

import os
from dataclasses import dataclass, field
from typing import List, Optional

import httpx
from fastapi import HTTPException, status
from jose import JWTError, jwt

# --- Configuration (from environment) ---------------------------------------

SERVER_URL = os.getenv("KEYCLOAK_SERVER_URL", "").rstrip("/")
REALM = os.getenv("KEYCLOAK_REALM", "")
CLIENT_ID = os.getenv("KEYCLOAK_CLIENT_ID", "")
CLIENT_SECRET = os.getenv("KEYCLOAK_CLIENT_SECRET", "")

# The client role that marks a developer (override the name via env if your
# Keycloak uses a different one). "user" is the plain-user role.
DEVELOPER_ROLE = os.getenv("KEYCLOAK_DEVELOPER_ROLE", "dev")
USER_ROLE = "user"

ISSUER = f"{SERVER_URL}/realms/{REALM}" if SERVER_URL and REALM else ""
TOKEN_URL = f"{ISSUER}/protocol/openid-connect/token"
JWKS_URL = f"{ISSUER}/protocol/openid-connect/certs"

_credentials_exception = HTTPException(
    status_code=status.HTTP_401_UNAUTHORIZED,
    detail="Could not validate credentials",
    headers={"WWW-Authenticate": "Bearer"},
)


@dataclass
class CurrentUser:
    """The authenticated principal for a request: Keycloak claims + local row."""

    sub: str
    email: Optional[str]
    username: Optional[str]
    roles: List[str] = field(default_factory=list)
    db_user: object = None  # models.User, kept loose to avoid a circular import

    @property
    def is_developer(self) -> bool:
        return DEVELOPER_ROLE in self.roles

    @property
    def id(self):
        return self.db_user.id if self.db_user is not None else None


# --- JWKS handling ----------------------------------------------------------

_jwks_cache: Optional[dict] = None


def get_jwks(force_refresh: bool = False) -> dict:
    """Fetch and cache the realm's JWKS. Refreshed on demand (e.g. key rotation)."""
    global _jwks_cache
    if _jwks_cache is None or force_refresh:
        resp = httpx.get(JWKS_URL, timeout=10)
        resp.raise_for_status()
        _jwks_cache = resp.json()
    return _jwks_cache


def _find_key(kid: str) -> Optional[dict]:
    # Try the cached keys first; if the kid is unknown (e.g. Keycloak rotated its
    # signing keys), refresh once and look again before giving up.
    for keys in (get_jwks().get("keys", []), get_jwks(force_refresh=True).get("keys", [])):
        for key in keys:
            if key.get("kid") == kid:
                return key
    return None


def verify_token(token: str) -> dict:
    """Validate a Keycloak access token and return its claims, or raise 401."""
    if not ISSUER:
        # Misconfiguration: fail closed rather than accept unverified tokens.
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Keycloak is not configured on the server",
        )
    try:
        header = jwt.get_unverified_header(token)
    except JWTError:
        raise _credentials_exception

    key = _find_key(header.get("kid", ""))
    if key is None:
        raise _credentials_exception

    try:
        claims = jwt.decode(
            token,
            key,
            algorithms=["RS256"],
            issuer=ISSUER,
            # Keycloak access tokens default aud to "account"; we validate the
            # issuer + signature and the authorized party (azp) instead.
            options={"verify_aud": False},
        )
    except JWTError:
        raise _credentials_exception

    # Ensure the token was issued for our client.
    if claims.get("azp") not in (None, CLIENT_ID) and CLIENT_ID not in claims.get(
        "aud", []
    ):
        raise _credentials_exception

    return claims


def roles_from_claims(claims: dict) -> List[str]:
    """Client roles for this app, from resource_access.<client_id>.roles."""
    resource_access = claims.get("resource_access", {}) or {}
    client = resource_access.get(CLIENT_ID, {}) or {}
    return list(client.get("roles", []))
