# WeForming App Store — Administrator Guide

Deployment, configuration, and operations for the WeForming App Store. For day-to-day
use of the app, see the [User Manual](MANUAL.md). For Keycloak specifics, see
[KEYCLOAK.md](KEYCLOAK.md); for the local/test stacks, see [TESTING.md](TESTING.md).

- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Configuration (`.env`)](#configuration-env)
- [Identity & access (Keycloak)](#identity--access-keycloak)
- [Production deployment (nginx + HTTPS)](#production-deployment-nginx--https)
- [Pre-flight with a bundled Keycloak](#pre-flight-with-a-bundled-keycloak)
- [Database & persistence](#database--persistence)
- [Managing platform types](#managing-platform-types)
- [Operations](#operations)
- [Security notes](#security-notes)
- [Troubleshooting](#troubleshooting)
- [Reference: files & ports](#reference-files--ports)

---

## Architecture

| Component | Tech | Notes |
| --- | --- | --- |
| **Frontend** | Vue 3 + Vuetify (built, served by nginx) | Static SPA. `VITE_API_URL` is baked in at build time. |
| **Backend** | FastAPI (Uvicorn) | REST API under `/api`. Validates Keycloak tokens. |
| **Database** | PostgreSQL 16 | Persistent, bind-mounted to `./data/postgres`. |
| **Identity** | Keycloak (external in production) | Login via Direct Access Grant, proxied by the backend. |
| **Reverse proxy** | nginx (on the host) | Terminates TLS, single origin for SPA + API. |

**Login model:** the browser never talks to Keycloak directly. The frontend posts
credentials to the backend's `/api/token`, which proxies to Keycloak (ROPC) and returns
a Keycloak access token. The backend then validates that token offline against the
realm's JWKS. This means **Keycloak only needs to be reachable from the backend**, not
from the public internet.

---

## Prerequisites

- A Linux host with **Docker** and **Docker Compose v2**.
- A **domain name (FQDN)** pointing at the host, and a **TLS certificate** (e.g.
  Let's Encrypt).
- **nginx** on the host (or another reverse proxy).
- A **Keycloak** instance for production (or use the bundled one for a pre-flight — see
  below).

---

## Configuration (`.env`)

Configuration lives in `.env` at the repo root. It is git-ignored; the repo ships a committed
template, **`.env_default`** — copy it and fill in your values:

```bash
cp .env_default .env
```

Values:

| Variable | Used by | Example | Notes |
| --- | --- | --- | --- |
| `VITE_API_URL` | frontend **build** | `https://apps.example.com` | Public origin of the API. **Rebuild the frontend after changing.** |
| `CORS_ORIGINS` | backend | `https://apps.example.com` | Comma-separated allowed browser origins. With a single-origin nginx setup, set it to the public origin. |
| `KEYCLOAK_SERVER_URL` | backend | `https://kc.example.com` | Base URL, no trailing slash. |
| `KEYCLOAK_REALM` | backend | `weforming` | |
| `KEYCLOAK_CLIENT_ID` | backend | `weforming-app-store` | Confidential client. |
| `KEYCLOAK_CLIENT_SECRET` | backend | *(secret)* | Keep private. |
| `POSTGRES_USER` / `POSTGRES_PASSWORD` / `POSTGRES_DB` | db + backend | `weforming` | Default `weforming` if unset. |
| `PUBLIC_BASE_URL` | backend (optional) | `https://apps.example.com` | If set, a `confirm_url` is included in the install payload sent to developers' Install URLs. |

The backend's database URL is assembled from the `POSTGRES_*` values in
`docker-compose.yml`; you normally don't set `SQLALCHEMY_DATABASE_URL` by hand in
production.

---

## Identity & access (Keycloak)

Full setup is in [KEYCLOAK.md](KEYCLOAK.md). In short, in your realm:

1. **Enable "Login with email"** (Realm settings → Login) so users sign in with email.
2. Create a **confidential client** (e.g. `weforming-app-store`) with **Direct access
   grants enabled**; copy its **client secret** into `.env`.
3. Define two **client roles** on that client:
   - `dev` — may publish apps and add platform types (name configurable via the backend
     env `KEYCLOAK_DEVELOPER_ROLE`, default `dev`).
   - `user` — regular user.
4. **Assign roles** to users (every user gets `user`; app authors also get `dev`).

**User management** (create users, reset passwords, assign roles) is done entirely in
Keycloak — the app store has no separate user database. On first login, the store
automatically creates a lightweight local profile keyed to the Keycloak user and seeds
two demo houses.

---

## Production deployment (nginx + HTTPS)

The production stack is `docker-compose.yml` (db + backend + frontend) pointed at your
**external** Keycloak. nginx terminates TLS and serves everything on one origin.

**1. Configure `.env`** — copy the template (`cp .env_default .env`), then set your FQDN and
Keycloak (see the table above):

```ini
VITE_API_URL=https://apps.example.com
CORS_ORIGINS=https://apps.example.com
KEYCLOAK_SERVER_URL=https://kc.example.com
KEYCLOAK_REALM=weforming
KEYCLOAK_CLIENT_ID=weforming-app-store
KEYCLOAK_CLIENT_SECRET=…
```

**2. Configure nginx.** Copy [deploy/nginx.conf](../deploy/nginx.conf) to
`/etc/nginx/sites-available/weforming.conf`, set your `server_name` and the two
`ssl_certificate*` paths, then enable it:

```bash
ln -s /etc/nginx/sites-available/weforming.conf /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
```

nginx serves the SPA at `/` and proxies `/api/` to the backend. It does **not** proxy
Keycloak (the browser never uses it). The app containers publish only on
`127.0.0.1:8000` / `127.0.0.1:8080`, so nginx is the sole public entry point.

**3. Start the stack:**

```bash
docker compose up -d --build
```

**4. The database starts empty.** Only the platform-type catalog (`Docker`, `Web`) is
seeded automatically; apps appear as developers publish them and houses as users add
them. (For a ready-made demo catalog in non-production setups, see [Demo data](#demo-data).)

Open `https://apps.example.com` and sign in.

> If you change `VITE_API_URL`, rebuild the frontend:
> `docker compose up -d --build frontend`.

---

## Pre-flight with a bundled Keycloak

To validate the whole stack **before** wiring in an external Keycloak, overlay
`docker-compose.prod-keycloak.yml`, which adds a bundled Keycloak (dev mode) and a demo
"hook-catcher". See [TESTING.md](TESTING.md#production-pre-flight-bundled-keycloak-on-a-server-with-an-fqdn)
for the full walkthrough. Leave `KEYCLOAK_*` in `.env` as-is — the overlay overrides them.

```bash
docker compose -f docker-compose.yml -f docker-compose.prod-keycloak.yml up -d --build
```

> The bundled Keycloak runs in **dev mode** and is for pre-flight testing only — not
> for real production. Switch to your hardened external Keycloak (plain
> `docker compose up`) once the pre-flight passes.

---

## Database & persistence

- Postgres data is stored in **`./data/postgres`** (a bind mount, git-ignored). It
  survives `docker compose down` and `down -v`. To wipe the database, stop the stack and
  delete that directory.
- The database starts **empty** (only the `Docker`/`Web` platform catalog is auto-seeded).

### Demo data

For demos or local testing, populate a sample app catalog and a couple of demo houses:

```bash
docker compose -f docker-compose.keycloak.yml exec backend python seed_demo.py
```

It reads `backend/demo_data.json` and refuses to run if apps already exist (pass `--force`
to seed anyway). **Not for production** — production content comes from real developers and
users.

### Backups

```bash
# Backup (adjust the service/compose file to your stack)
docker compose exec -T db pg_dump -U weforming weforming > backup_$(date +%F).sql

# Restore into an empty database
cat backup_2026-01-01.sql | docker compose exec -T db psql -U weforming -d weforming
```

Because app icons and building photos are stored **in the database**, a Postgres dump is
a complete backup — there are no separate media files to archive.

---

## Managing platform types

Platform types (e.g. *Docker*, *Web*) are a catalog table. `Docker` and `Web` are seeded
automatically. They are **extensible**:

- **In the app:** a developer can add a new type (it then appears for selection on apps
  and houses).
- **Via the API:** `POST /api/platform-types/` with `{"name":"…"}` and a developer's
  bearer token.
- **Directly in the DB:** `INSERT INTO platform_types (name) VALUES ('IoT');`

Assets reference platform types through a join table (`asset_platforms`); apps reference
one by name (`platform_type`). An app is installable on a house only if the house
supports the app's platform type.

---

## Operations

```bash
# Start / rebuild
docker compose up -d --build

# Rebuild a single service after a code change
docker compose up -d --build backend
docker compose up -d --build frontend

# Logs
docker compose logs -f backend
docker compose logs -f frontend

# Stop (keeps ./data/postgres)
docker compose down

# Status
docker compose ps
```

**Updating the app:** pull the new code, then `docker compose up -d --build`. The
backend applies lightweight schema migrations automatically on startup (adding new
columns/tables and seeding platform types) — no manual migration step is required for
upgrades. It also waits for the database to accept connections before starting.

---

## Security notes

- **Keep `.env` secret** — it holds the Keycloak client secret and DB password. It is
  git-ignored; don't commit it.
- **App containers bind to `127.0.0.1` only.** Do not expose ports 8000/8080 publicly;
  nginx (with TLS) is the public entry point.
- **The install-confirmation endpoint (`POST /api/installs/confirm`) is
  unauthenticated by design** — it is authorized by the per-install `secret_key` that
  the store sends to the developer's Install URL. This lets external installers report
  completion without credentials. It only allows flipping an install to
  finished/failed; it exposes no data.
- **The bundled Keycloak and the `hookcatcher` service are for testing/demos only** —
  never run them in production.
- **Upload size:** nginx `client_max_body_size` (25M in the sample config) caps icon and
  building-photo uploads; adjust if needed.
- Consider running Keycloak and Postgres on a private network reachable only by the
  backend.

---

## Troubleshooting

| Symptom | Likely cause / fix |
| --- | --- |
| Backend restarts / `connection refused` to `db` | DB still starting. The backend retries automatically; if it persists, check the `db` container is healthy (`docker compose ps`). |
| Login fails for valid users | Check `KEYCLOAK_*` values, that the client has **Direct access grants** enabled, and that **Login with email** is on. |
| Login works but user has no developer powers | The user is missing the `dev` **client role** in Keycloak (or `KEYCLOAK_DEVELOPER_ROLE` names a different role). |
| Browser console shows CORS errors | `CORS_ORIGINS` must include the exact public origin (scheme + host). With nginx single-origin this shouldn't occur. |
| Frontend calls the wrong API URL | `VITE_API_URL` is baked in at build time — rebuild the frontend after changing it. |
| Map tiles don't load | The browser needs internet access to reach OpenStreetMap tiles. |
| `seed_demo.py` aborts | The DB already has apps; that's the safety guard. Use `--force` to seed anyway. |

---

## Reference: files & ports

**Compose files**

| File | Purpose | Public ports |
| --- | --- | --- |
| `docker-compose.yml` | Production (external Keycloak). | backend `127.0.0.1:8000`, frontend `127.0.0.1:8080` (front them with nginx) |
| `docker-compose.prod-keycloak.yml` | Overlay adding a bundled Keycloak + hook-catcher for pre-flight. | Keycloak `127.0.0.1:8081` |
| `docker-compose.keycloak.yml` | Full local test stack. | UI `8090`, API `8001`, Keycloak `8081` |

**Key files**

| Path | What |
| --- | --- |
| `.env_default` | Committed config template — copy to `.env`. |
| `.env` | Local configuration (secret; git-ignored). |
| `deploy/nginx.conf` | Sample nginx reverse-proxy config. |
| `keycloak/realm-weforming.json` | Realm import for the bundled/test Keycloak. |
| `backend/seed_demo.py` + `demo_data.json` | Optional demo-data seeder (apps + houses). |
| `data/postgres/` | Persistent database (bind mount; git-ignored). |
| `docs/KEYCLOAK.md` | Keycloak realm/client/role setup. |
| `docs/TESTING.md` | Local test stack + pre-flight walkthrough. |
| `docs/MANUAL.md` | End-user manual. |
