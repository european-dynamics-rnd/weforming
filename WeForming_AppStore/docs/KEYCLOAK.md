# Keycloak setup

The app store uses an **external Keycloak** as its identity provider. Two classes of
users are supported, distinguished by **client roles**:

- `dev` — can publish/create apps (sees the "Publish a New App" entry, can call
  `POST /api/apps/`). The name is configurable via the backend env `KEYCLOAK_DEVELOPER_ROLE`
  (default `dev`).
- `user` — plain user; can browse and install apps only.

## 1. Realm

Create (or reuse) a realm, e.g. `weforming`.

- **Realm settings → Login → Login with email**: **On**. This lets users sign in with their
  email in the app's login form (the form value is sent to Keycloak as the username).

## 2. Client

Create a client for the app store:

- **Client ID**: e.g. `weforming-app-store`
- **Client authentication**: **On** (this makes it a *confidential* client with a secret)
- **Authentication flow → Direct access grants**: **Enabled** (required for the login form / ROPC)
- After saving, open **Credentials** and copy the **Client secret**.

## 3. Client roles

On the client, go to **Roles** and create two roles:

- `dev`
- `user`

Assign roles to users (**Users → \<user\> → Role mapping → Assign role → Filter by clients**):

- Give every app-store user the `user` role.
- Also give app authors the `dev` role.

> Roles are read from the access token's `resource_access.<client_id>.roles` claim. Keycloak
> includes this claim automatically for clients the user has roles in.

## 4. Backend configuration

Set these environment variables for the backend in `.env` (copy it from `.env_default` first):

| Variable                  | Example                              |
| ------------------------- | ------------------------------------ |
| `KEYCLOAK_SERVER_URL`     | `https://keycloak.example.com`       |
| `KEYCLOAK_REALM`          | `weforming`                          |
| `KEYCLOAK_CLIENT_ID`      | `weforming-app-store`                |
| `KEYCLOAK_CLIENT_SECRET`  | *(the client secret from step 2)*    |

`KEYCLOAK_SERVER_URL` must have **no trailing slash**. The backend derives the token, JWKS,
and issuer URLs from these values:

- Issuer: `{SERVER_URL}/realms/{REALM}`
- Token:  `{issuer}/protocol/openid-connect/token`
- JWKS:   `{issuer}/protocol/openid-connect/certs`

## How it works

- The frontend login form posts username/password to our own `POST /api/token`, which proxies
  to Keycloak's Direct Access Grant and returns the Keycloak-issued access token. The client
  secret never reaches the browser.
- Every protected request carries that Keycloak access token. The backend validates it offline
  against the realm's JWKS (RS256, issuer + signature + expiry) — no shared secret.
- On first login, a local `users` row is provisioned automatically, keyed by the Keycloak
  subject (`sub`), so assets and installed apps still reference a stable local user id.

## Notes

- Users are created and managed **in Keycloak** — there is no local registration anymore.
- If you rotate the client secret, update `KEYCLOAK_CLIENT_SECRET` and restart the backend.
