# WeForming App Store

An **app store for buildings** as part of the WeForming project. The users can manage several buildings ("houses"),
browse a catalog of apps, and install the ones they want onto a house. Developers publish their own apps and provide an install endpoint; the store connects the two and
tracks each installation. It ties into the WeForming ecosystem by providing the users with easy means to track and install the apps related to their assets. Identities are managed by Keycloak and we include a small demo config for you to test, as well as a bundle of sample data.

> Start with **[Getting Started](docs/GETTING-STARTED.md)** — a two-minute intro.

---

##  Documentation

| Document | For | What's inside |
| --- | --- | --- |
| **[Getting Started](docs/GETTING-STARTED.md)** | Everyone | A simple, high-level tour of what the app store does. |
| **[User Manual](docs/MANUAL.md)** | Users & developers | Full walkthrough: houses, browsing, installing, My Apps, and publishing apps. |
| **[Install Flow](docs/install-flow.md)** | App developers | A step-by-step diagram of how an install reaches your system. |
| **[Administrator Guide](docs/ADMIN.md)** | Admins / ops | Architecture, configuration, deployment (nginx + HTTPS), backups, operations. |
| **[Keycloak Setup](docs/KEYCLOAK.md)** | Admins | Realm, client, and role configuration for sign-in. |
| **[Testing & Pre-flight](docs/TESTING.md)** | Admins / developers | Running the local test stack and the production pre-flight. |

---

## What it does, in brief

- Each **house** has a location and a list of **platforms** it supports (e.g. *Docker*, *Web*).
- Each **app** targets a platform. The store only lets you install an app onto a house that
  supports it.
- Installing an app **POSTs the details to the developer's install URL**; the developer's
  system does the work and **calls back** to mark it finished or failed. Installs are
  asynchronous and tracked in **My Apps**.
- Two roles: **users** (browse & install) and **developers** (also publish apps).

---

## Tech stack

| Layer | Technology |
| --- | --- |
| Frontend | Vue 3 + Vuetify (SPA), Leaflet map |
| Backend | FastAPI (Python), served by Uvicorn |
| Database | PostgreSQL (app icons & building photos stored in the DB) |
| Identity | Keycloak (login proxied by the backend) |
| Reverse proxy | nginx (TLS, single origin) |
| Packaging | Docker + Docker Compose |

---

## Try it locally

The self-contained test stack bundles everything (Keycloak + Postgres + backend + frontend), and by default, it includes some demo app and demo users for you to try it out locally if you wish:

```bash
docker compose -f docker-compose.keycloak.yml up -d --build
# optional — load a demo catalog + houses (the DB is empty otherwise):
docker compose -f docker-compose.keycloak.yml exec backend python seed_demo.py
```

Then open **http://localhost:8090** and sign in with a demo account:

| User | Password | Role |
| --- | --- | --- |
| `dev@weforming.eu` | `devpass` | developer |
| `user@weforming.eu` | `userpass` | user |

See **[docs/TESTING.md](docs/TESTING.md)** for details, and **[docs/ADMIN.md](docs/ADMIN.md)**
for production deployment.

---

## Repository layout

```
backend/     FastAPI app (models, schemas, CRUD, auth, migrations)
frontend/    Vue 3 + Vuetify single-page app
deploy/      nginx.conf (reverse proxy) + hookcatcher.py (test helper)
keycloak/    realm import for the bundled/test Keycloak
docs/        documentation (start with GETTING-STARTED.md)
docker-compose.yml                  production stack (external Keycloak)
docker-compose.prod-keycloak.yml    overlay: bundled Keycloak for pre-flight
docker-compose.keycloak.yml         full local test stack
```