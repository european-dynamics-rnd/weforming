import time
from typing import List
from sqlalchemy import inspect, text
from sqlalchemy.exc import OperationalError

from fastapi import FastAPI, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from fastapi.middleware.cors import CORSMiddleware
from fastapi import UploadFile, File, Form
from fastapi.responses import Response
import os
import json
import uuid
import secrets
import httpx
from sqlalchemy.orm import Session
from schemas import (
    AppCreate, App, Asset as AssetSchema, Me,
    PlatformType as PlatformTypeSchema, PlatformTypeCreate,
    UserAppCreate, UserAppDetail, InstallStatus, InstallConfirm,
)
from crud import (
    create_app,
    create_asset,
    get_asset,
    delete_asset,
    get_assets_by_user,
    get_platform_types,
    get_or_create_platform_type,
    resolve_platform_types,
    search_apps,
    get_or_create_keycloak_user,
    get_app,
    get_apps,
    get_user_apps,
    get_user_app_by_uuid,
    create_user_app,
)
from database import SessionLocal, engine, Base
import models  # noqa: F401 — registers the ORM models on Base.metadata
import auth
from auth import CurrentUser

# --- Wait for the database to accept connections -----------------------------
# The DB container may still be starting (or restarting right after first-run
# initdb) when the backend boots, so retry briefly instead of crashing.
def _wait_for_db(retries: int = 30, delay: float = 1.0):
    for attempt in range(1, retries + 1):
        try:
            with engine.connect():
                return
        except OperationalError:
            if attempt == retries:
                raise
            time.sleep(delay)


_wait_for_db()

# --- Create tables, then apply lightweight column migrations -----------------
# create_all must run *after* models are imported so Base.metadata is populated;
# otherwise a fresh database (e.g. a new Postgres) would get no tables.
Base.metadata.create_all(bind=engine)

_insp = inspect(engine)
_BINARY = "BYTEA" if engine.dialect.name == "postgresql" else "BLOB"
_FLOAT = "DOUBLE PRECISION" if engine.dialect.name == "postgresql" else "REAL"


def _ensure_column(table: str, column: str, ddl_type: str):
    """Add a column to an older database that predates it (dialect-agnostic)."""
    if table in _insp.get_table_names():
        existing = {c["name"] for c in _insp.get_columns(table)}
        if column not in existing:
            with engine.begin() as conn:
                conn.execute(text(f"ALTER TABLE {table} ADD COLUMN {column} {ddl_type}"))


def _migrate_asset_platforms():
    """Move assets.platforms from legacy text to a JSON dict of supported platforms.

    Older rows stored a (usually empty) comma-separated string. Empty capability
    sets are backfilled to support both platforms so existing demo houses work.
    """
    if "assets" not in _insp.get_table_names():
        return
    cols = {c["name"]: str(c["type"]).upper() for c in _insp.get_columns("assets")}
    if "platforms" not in cols:
        return  # fresh schema has no legacy `platforms` column — nothing to migrate
    col_type = cols["platforms"]
    if engine.dialect.name == "postgresql":
        if "JSON" in col_type:
            return  # already migrated
        with engine.begin() as conn:
            conn.execute(text(
                "UPDATE assets SET platforms='{}' WHERE platforms IS NULL OR btrim(platforms)='' "
                "OR left(btrim(platforms),1) NOT IN ('{','[')"
            ))
            conn.execute(text(
                "UPDATE assets SET platforms='{\"Docker\": true, \"Web\": true}' WHERE platforms='{}'"
            ))
            conn.execute(text("ALTER TABLE assets ALTER COLUMN platforms TYPE JSONB USING platforms::jsonb"))
    else:
        # SQLite (and others): JSON type reads TEXT via json.loads, so just ensure valid JSON.
        with engine.begin() as conn:
            conn.execute(text(
                "UPDATE assets SET platforms='{}' WHERE platforms IS NULL OR trim(platforms)='' "
                "OR substr(trim(platforms),1,1) NOT IN ('{','[')"
            ))


_ensure_column("apps", "platform_type", "VARCHAR")
_ensure_column("apps", "icon_data", _BINARY)
_ensure_column("apps", "icon_mime", "VARCHAR")
_ensure_column("apps", "developer_id", "INTEGER")
_ensure_column("apps", "developer_url", "VARCHAR")
_ensure_column("apps", "version", "VARCHAR")
_ensure_column("apps", "install_url", "VARCHAR")
_ensure_column("apps", "params_schema", "VARCHAR")
_ensure_column("users", "keycloak_id", "VARCHAR")
_ensure_column("user_apps", "install_uuid", "VARCHAR")
_ensure_column("user_apps", "secret_key", "VARCHAR")
_ensure_column("user_apps", "status", "VARCHAR")
_ensure_column("assets", "latitude", _FLOAT)
_ensure_column("assets", "longitude", _FLOAT)
_ensure_column("assets", "image_data", _BINARY)
_ensure_column("assets", "image_mime", "VARCHAR")
_migrate_asset_platforms()


def _legacy_platform_names(raw):
    """Extract platform names from a legacy assets.platforms value (dict/list/str)."""
    if raw is None:
        return []
    if isinstance(raw, dict):
        return [k for k, v in raw.items() if v]
    if isinstance(raw, list):
        return [str(x) for x in raw]
    if isinstance(raw, str):
        try:
            d = json.loads(raw)
        except (ValueError, TypeError):
            return [p.strip() for p in raw.split(",") if p.strip()]
        if isinstance(d, dict):
            return [k for k, v in d.items() if v]
        if isinstance(d, list):
            return [str(x) for x in d]
    return []


def _seed_and_migrate_platform_types():
    """Seed the platform-type catalog and migrate legacy JSON platforms into the
    asset↔platform_type join (one time)."""
    from models import PlatformType, Asset as _Asset
    has_legacy = "assets" in _insp.get_table_names() and \
        "platforms" in {c["name"] for c in _insp.get_columns("assets")}
    with SessionLocal() as db:
        for nm in ("Docker", "Web"):
            if not db.query(PlatformType).filter(PlatformType.name == nm).first():
                db.add(PlatformType(name=nm))
        db.commit()

        already = db.execute(text("SELECT COUNT(*) FROM asset_platforms")).scalar()
        if already == 0 and has_legacy:
            by_name = {pt.name: pt for pt in db.query(PlatformType).all()}
            for asset in db.query(_Asset).all():
                raw = db.execute(text("SELECT platforms FROM assets WHERE id=:i"), {"i": asset.id}).scalar()
                pts = [by_name[n] for n in _legacy_platform_names(raw) if n in by_name]
                if pts:
                    asset.platform_types = pts
            db.commit()


_seed_and_migrate_platform_types()

# Installs that predate the async flow are considered finished.
if "user_apps" in _insp.get_table_names():
    with engine.begin() as _conn:
        _conn.execute(text("UPDATE user_apps SET status='finished' WHERE status IS NULL"))


# --- App setup ---------------------------------------------------------------
app = FastAPI()

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="api/token")


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


def get_current_user(
    token: str = Depends(oauth2_scheme), db: Session = Depends(get_db)
) -> CurrentUser:
    """Validate the Keycloak access token and JIT-provision the local user row."""
    claims = auth.verify_token(token)
    sub = claims.get("sub")
    if not sub:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Could not validate credentials",
            headers={"WWW-Authenticate": "Bearer"},
        )
    email = claims.get("email")
    db_user = get_or_create_keycloak_user(db, keycloak_id=sub, email=email)
    return CurrentUser(
        sub=sub,
        email=email,
        username=claims.get("preferred_username"),
        roles=auth.roles_from_claims(claims),
        db_user=db_user,
    )


def require_developer(current_user: CurrentUser = Depends(get_current_user)) -> CurrentUser:
    """Guard for developer-only endpoints (publishing apps)."""
    if not current_user.is_developer:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Developer role required",
        )
    return current_user


# CORS — allowed browser origins are configurable (comma-separated) via env.
_default_origins = "http://frontend:8080,http://localhost:8080"
cors_origins = [o.strip() for o in os.getenv("CORS_ORIGINS", _default_origins).split(",") if o.strip()]
app.add_middleware(
    CORSMiddleware,
    allow_origins=cors_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.post("/api/token")
def login_for_access_token(form_data: OAuth2PasswordRequestForm = Depends()):
    """Proxy the login form to Keycloak's Direct Access Grant (ROPC).

    The frontend contract is unchanged — it still posts username/password here and
    receives ``{access_token, token_type}`` — but the credentials are verified by
    Keycloak and the returned token is a Keycloak-signed access token.
    """
    try:
        resp = httpx.post(
            auth.TOKEN_URL,
            data={
                "grant_type": "password",
                "client_id": auth.CLIENT_ID,
                "client_secret": auth.CLIENT_SECRET,
                "username": form_data.username,
                "password": form_data.password,
                "scope": "openid",
            },
            timeout=15,
        )
    except httpx.HTTPError:
        raise HTTPException(status_code=503, detail="Authentication service unavailable")

    if resp.status_code != 200:
        raise HTTPException(status_code=400, detail="Incorrect email or password")

    return resp.json()


@app.get("/api/me", response_model=Me)
def read_me(current_user: CurrentUser = Depends(get_current_user)):
    return Me(
        email=current_user.email,
        username=current_user.username,
        roles=current_user.roles,
        is_developer=current_user.is_developer,
    )


def _to_float(v):
    try:
        return float(v) if v not in (None, "") else None
    except (ValueError, TypeError):
        return None


def _parse_platform_names(platforms: str):
    try:
        data = json.loads(platforms or "[]")
    except (ValueError, TypeError):
        return []
    if isinstance(data, list):
        return [str(x) for x in data]
    if isinstance(data, dict):
        return [k for k, v in data.items() if v]
    return []


@app.get("/api/platform-types/", response_model=List[PlatformTypeSchema])
def read_platform_types(db: Session = Depends(get_db)):
    return get_platform_types(db)


@app.post("/api/platform-types/", response_model=PlatformTypeSchema)
def create_platform_type_endpoint(payload: PlatformTypeCreate, current_user: CurrentUser = Depends(require_developer), db: Session = Depends(get_db)):
    name = (payload.name or "").strip()
    if not name:
        raise HTTPException(status_code=400, detail="Name is required")
    return get_or_create_platform_type(db, name)


@app.get("/api/assets/", response_model=List[AssetSchema])
def read_assets(current_user: CurrentUser = Depends(get_current_user), db: Session = Depends(get_db)):
    return get_assets_by_user(db, user_id=current_user.id)


@app.post("/api/assets/", response_model=AssetSchema)
async def create_asset_endpoint(
    name: str = Form(...),
    address: str = Form(""),
    latitude: str = Form(""),
    longitude: str = Form(""),
    platforms: str = Form("[]"),
    image: UploadFile = File(None),
    current_user: CurrentUser = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    image_data, image_mime = None, None
    if image is not None and (image.filename or "").strip():
        image_data = await image.read()
        image_mime = image.content_type or "application/octet-stream"
    return create_asset(
        db, user_id=current_user.id, name=name, address=address,
        latitude=_to_float(latitude), longitude=_to_float(longitude),
        platform_names=_parse_platform_names(platforms),
        image_data=image_data, image_mime=image_mime,
    )


@app.put("/api/assets/{asset_id}", response_model=AssetSchema)
async def update_asset_endpoint(
    asset_id: int,
    name: str = Form(...),
    address: str = Form(""),
    latitude: str = Form(""),
    longitude: str = Form(""),
    platforms: str = Form("[]"),
    image: UploadFile = File(None),
    current_user: CurrentUser = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    asset = get_asset(db, asset_id)
    if asset is None or asset.user_id != current_user.id:
        raise HTTPException(status_code=404, detail="Asset not found")
    asset.name = name
    asset.address = address
    asset.latitude = _to_float(latitude)
    asset.longitude = _to_float(longitude)
    asset.platform_types = resolve_platform_types(db, _parse_platform_names(platforms))
    if image is not None and (image.filename or "").strip():
        asset.image_data = await image.read()
        asset.image_mime = image.content_type or "application/octet-stream"
    db.commit()
    db.refresh(asset)
    return asset


@app.delete("/api/assets/{asset_id}")
def delete_asset_endpoint(asset_id: int, current_user: CurrentUser = Depends(get_current_user), db: Session = Depends(get_db)):
    asset = get_asset(db, asset_id)
    if asset is None or asset.user_id != current_user.id:
        raise HTTPException(status_code=404, detail="Asset not found")
    delete_asset(db, asset)
    return {"deleted": asset_id}


@app.get("/api/assets/{asset_id}/image")
def read_asset_image(asset_id: int, db: Session = Depends(get_db)):
    asset = get_asset(db, asset_id)
    if asset is None or not asset.image_data:
        raise HTTPException(status_code=404, detail="Image not found")
    return Response(content=asset.image_data, media_type=asset.image_mime or "application/octet-stream")


@app.get("/api/apps/", response_model=List[App])
def read_apps(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    return get_apps(db, skip=skip, limit=limit)


@app.get("/api/apps/search/", response_model=List[App])
def search_apps_endpoint(q: str, db: Session = Depends(get_db)):
    return search_apps(db, query=q)


@app.post("/api/apps/", response_model=App)
async def create_app_endpoint(
    name: str = Form(...),
    description: str = Form(""),
    rating: int = Form(0),
    price: int = Form(0),
    platform_type: str = Form(""),
    developer_url: str = Form(""),
    version: str = Form(""),
    install_url: str = Form(""),
    params_schema: str = Form(""),
    icon: UploadFile = File(None),
    current_user: CurrentUser = Depends(require_developer),
    db: Session = Depends(get_db),
):
    icon_data = None
    icon_mime = None
    if icon is not None and (icon.filename or "").strip():
        icon_data = await icon.read()
        icon_mime = icon.content_type or "application/octet-stream"

    app_data = AppCreate(
        name=name,
        description=description,
        rating=rating,
        price=price,
        platform_type=platform_type,
        developer_url=developer_url,
        version=version,
        install_url=install_url,
        params_schema=params_schema,
    )
    return create_app(
        db=db,
        app=app_data,
        icon_data=icon_data,
        icon_mime=icon_mime,
        developer_id=current_user.id,
    )


@app.get("/api/apps/{app_id}", response_model=App)
def read_app(app_id: int, db: Session = Depends(get_db)):
    db_app = get_app(db, app_id=app_id)
    if db_app is None:
        raise HTTPException(status_code=404, detail="App not found")
    return db_app


@app.get("/api/apps/{app_id}/icon")
def read_app_icon(app_id: int, db: Session = Depends(get_db)):
    db_app = get_app(db, app_id=app_id)
    if db_app is None or not db_app.icon_data:
        raise HTTPException(status_code=404, detail="Icon not found")
    return Response(
        content=db_app.icon_data,
        media_type=db_app.icon_mime or "application/octet-stream",
    )


@app.get("/api/user-apps/", response_model=List[UserAppDetail])
def read_user_apps(current_user: CurrentUser = Depends(get_current_user), db: Session = Depends(get_db)):
    return get_user_apps(db, user_id=current_user.id)


@app.post("/api/user-apps/", response_model=UserAppDetail)
def install_app(user_app: UserAppCreate, current_user: CurrentUser = Depends(get_current_user), db: Session = Depends(get_db)):
    """Start an install: generate a unique id + secret, add them to the JSON Form
    content, persist as in_progress, and POST the augmented content to the app's
    install_url. The install finishes asynchronously when the installer calls
    POST /api/installs/confirm with the id + secret.
    """
    install_uuid = str(uuid.uuid4())
    secret_key = secrets.token_urlsafe(32)

    # Augment the JSON Form content with the generated identifiers.
    try:
        config = json.loads(user_app.json_config or "{}")
    except (ValueError, TypeError):
        config = {}
    if not isinstance(config, dict):
        config = {"value": config}
    config["install_uuid"] = install_uuid
    config["secret_key"] = secret_key
    public_base = os.getenv("PUBLIC_BASE_URL", "").rstrip("/")
    if public_base:
        config["confirm_url"] = f"{public_base}/api/installs/confirm"

    db_user_app = create_user_app(
        db, user_id=current_user.id, user_app=user_app,
        json_config=json.dumps(config),
        install_uuid=install_uuid, secret_key=secret_key, status="in_progress",
    )

    # Kick off the external install with the augmented content (best-effort).
    db_app = get_app(db, app_id=user_app.app_id)
    if db_app is not None and db_app.install_url:
        try:
            httpx.post(db_app.install_url, json=config, timeout=15)
        except httpx.HTTPError:
            pass

    return db_user_app


@app.get("/api/installs/{install_uuid}", response_model=InstallStatus)
def get_install_status(install_uuid: str, current_user: CurrentUser = Depends(get_current_user), db: Session = Depends(get_db)):
    """Poll the status of one of the current user's installs."""
    ua = get_user_app_by_uuid(db, install_uuid)
    if ua is None or ua.user_id != current_user.id:
        raise HTTPException(status_code=404, detail="Install not found")
    return InstallStatus(install_uuid=ua.install_uuid, status=ua.status)


@app.post("/api/installs/confirm", response_model=InstallStatus)
def confirm_install(payload: InstallConfirm, db: Session = Depends(get_db)):
    """Unauthenticated callback for the external installer. The secret (sent with the
    install payload) authorizes flipping the install to 'finished'."""
    ua = get_user_app_by_uuid(db, payload.install_uuid)
    if ua is None or not ua.secret_key or not secrets.compare_digest(ua.secret_key, payload.secret_key):
        raise HTTPException(status_code=404, detail="Unknown install or invalid secret")
    ua.status = "finished" if payload.success else "failed"
    db.commit()
    return InstallStatus(install_uuid=ua.install_uuid, status=ua.status)
