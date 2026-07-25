# Manual testing

A self-contained local stack (Keycloak + Postgres + backend + frontend) for clicking
through the app end to end. It is defined in
[docker-compose.keycloak.yml](../docker-compose.keycloak.yml) and is separate from the
production `docker-compose.yml` (which expects an external Keycloak).

## Start the stack

```bash
cd /e/dev/weforming/weforming_app_store

# build + start everything
docker compose -f docker-compose.keycloak.yml up -d --build
```

Wait ~10s, then open **http://localhost:8090**.

| Service     | URL                        | Notes                    |
| ----------- | -------------------------- | ------------------------ |
| App UI      | http://localhost:8090      | what you click through   |
| Backend API | http://localhost:8001      | e.g. `/api/apps/`        |
| Keycloak    | http://localhost:8081      | admin console: admin / admin |

### Test users (log in with the email as username)

| User                | Password  | Role      | Can publish apps? |
| ------------------- | --------- | --------- | ----------------- |
| `dev@weforming.eu`  | `devpass` | developer | yes               |
| `user@weforming.eu` | `userpass`| user      | no                |

## What to test

1. **Login / roles** — sign in as each user. The developer sees a **Publish a New App**
   button on the home page; the plain user does not and is redirected away from `/create-app`.
2. **Publish** (as developer) — fill in name, platform type, version, developer link (URL),
   install URL, and optionally paste a JSON Schema into *Installation parameters*, then
   **Publish New App**. The new app appears on the home page with its developer link and version.
3. **Capability matching** — open a **Docker** app and click **Install**. Step 2 should show
   *"House 1 supports the Docker installation type"* and let you continue. Each house's supported
   platforms are stored as a JSON dict (`{"Docker": true, "Web": true}`).
4. **Parameters step (JSON Forms)** — if the app has an installation-parameters schema, step 3
   renders it as a form. Fill it in and continue to the review step.
5. **Install hook** — on install, the backend POSTs the collected parameters to the app's
   **Install URL**. To watch the payload arrive, see [Watch install payloads](#watch-install-payloads).
6. **Icons** — app icons are stored in and served from the database
   (`GET /api/apps/{id}/icon`).

## Quick API checks (optional)

```bash
# get a developer token (ROPC via the backend proxy to Keycloak)
TOKEN=$(curl -s -X POST http://localhost:8001/api/token \
  -d 'username=dev@weforming.eu&password=devpass' | python -c "import sys,json;print(json.load(sys.stdin)['access_token'])")

curl -s http://localhost:8001/api/me -H "Authorization: Bearer $TOKEN"   # roles / is_developer
curl -s http://localhost:8001/api/apps/                                   # app catalog
```

## Watch install payloads (hook-catcher)

A **`hookcatcher`** service ([deploy/hookcatcher.py](../deploy/hookcatcher.py)) is part of both the
test stack and the prod pre-flight. It logs whatever an app's Install URL POSTs to it, so you can
watch the JSON Form values (+ `install_uuid` + `secret_key`) arrive live. Point an app's
**Install URL** at `http://hookcatcher:8000/install` and tail its log:

```bash
docker compose -f docker-compose.keycloak.yml logs -f hookcatcher
```

The bundled **"Test Presentation APP"** is already set up this way: it has a single **IP address**
parameter in its install form and posts to the hook-catcher. Demo flow:

1. Log in as `dev@weforming.eu` / `devpass`, open **Test Presentation APP** → **Install**.
2. Fill in the **IP address** in the JSON-Forms step, finish the wizard → **Waiting…** screen.
3. In the hook-catcher log you'll see the payload; copy the `install_uuid` + `secret_key` and
   confirm completion (the unauthenticated installer callback):
   ```bash
   curl -X POST http://localhost:8001/api/installs/confirm -H "Content-Type: application/json" \
     -d '{"install_uuid":"<uuid>","secret_key":"<secret>"}'          # or add "success": false
   ```
4. The Waiting screen flips to success (or failure); **My Apps** shows the status chip.

## Data

- Postgres data lives in the gitignored **`./data/postgres`**, so it survives `down`/`up`.
- The database starts **empty**. To load a demo catalog + demo houses (aborts if apps
  already exist):
  ```bash
  docker compose -f docker-compose.keycloak.yml exec backend python seed_demo.py
  ```

## Stop / reset

```bash
# stop, keep data
docker compose -f docker-compose.keycloak.yml down

# rebuild a single service after code changes
docker compose -f docker-compose.keycloak.yml up -d --build backend
docker compose -f docker-compose.keycloak.yml up -d --build frontend

# full reset (wipe the database)
docker compose -f docker-compose.keycloak.yml down
rm -rf ./data/postgres
docker compose -f docker-compose.keycloak.yml up -d --build
docker compose -f docker-compose.keycloak.yml exec backend python seed_demo.py   # optional demo data
```

---

# Production pre-flight (bundled Keycloak, on a server with an FQDN)

This runs the **production** stack (`docker-compose.yml`: db + backend + frontend) with a
**bundled Keycloak** overlaid on top, so you can validate the whole setup on a real server
*before* wiring in your external Keycloak.

Why this works cleanly: login uses ROPC proxied by the backend, so the **browser never talks
to Keycloak** — only the backend does, over the internal Docker network. Keycloak therefore
needs no public hostname. Only the **backend** and **frontend** need your server's FQDN.

### 1. Point the config at your server (nginx + HTTPS, single origin)

nginx terminates TLS on your FQDN and proxies to the containers, so the SPA and API share
one origin (`https://<FQDN>`). Create `.env` from the template (`cp .env_default .env`) and edit
it (replace `apps.example.com` with your FQDN):

```ini
VITE_API_URL=https://apps.example.com
CORS_ORIGINS=https://apps.example.com
```

Leave the `KEYCLOAK_*` values as-is — the overlay overrides them to the bundled Keycloak.
The containers publish on **localhost only** (`127.0.0.1:8000` / `127.0.0.1:8080`); nginx is the
only public entry point. Keycloak (`127.0.0.1:8081`) is internal — reach the admin console via an
SSH tunnel if needed; the browser never uses it.

### 1b. nginx site

Copy [deploy/nginx.conf](../deploy/nginx.conf) to `/etc/nginx/sites-available/weforming.conf`,
set your FQDN and TLS cert paths, then:

```bash
ln -s /etc/nginx/sites-available/weforming.conf /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
```

It serves the SPA at `/` and proxies `/api/` to the backend. (No Keycloak location — the browser
never talks to it.)

### 2. Run it

```bash
docker compose -f docker-compose.yml -f docker-compose.prod-keycloak.yml up -d --build

# optional: load a demo catalog + houses into the (otherwise empty) DB
docker compose -f docker-compose.yml -f docker-compose.prod-keycloak.yml \
  exec backend python seed_demo.py
```

Open `https://<your-FQDN>` and log in with `dev@weforming.eu` / `devpass` (or
`user@weforming.eu` / `userpass`). If login and the app work here, the whole stack is sound.

> If `VITE_API_URL` changes, rebuild the frontend (it is baked in at build time):
> `docker compose -f docker-compose.yml -f docker-compose.prod-keycloak.yml up -d --build frontend`

### 3. Switch to the external Keycloak

Once the pre-flight passes, drop the overlay and point at the real Keycloak:

1. Fill in the `KEYCLOAK_*` values in `.env` (see [KEYCLOAK.md](KEYCLOAK.md)).
2. Run the plain production stack:
   ```bash
   docker compose up -d --build
   ```

Nothing else changes — the browser still only talks to the backend, and the backend now
validates tokens against your external Keycloak.

### Notes

- The bundled Keycloak uses dev mode (`start-dev`) — fine for this pre-flight, **not** for
  real production. In production, use your hardened external Keycloak.
- The pre-flight also runs the **`hookcatcher`** service, so the demo "Test Presentation APP"
  works here too. Watch install payloads with:
  ```bash
  docker compose -f docker-compose.yml -f docker-compose.prod-keycloak.yml logs -f hookcatcher
  ```
- If `VITE_API_URL` changes you must rebuild the frontend (it is baked in at build time).
- Optional: run uvicorn with `--proxy-headers` so it trusts nginx's `X-Forwarded-*`
  (not required by this app, which builds no absolute URLs from the request).
- The database still persists to the gitignored `./data/postgres`.