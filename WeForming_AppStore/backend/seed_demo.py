"""Explicit demo-data seeder for the WeForming App Store (demo mode only).

By default the app starts with an EMPTY database (no apps, no houses). Run this
script to populate a demo catalog and a couple of demo houses — analogous to the
Keycloak demo realm import. It is never run automatically.

    docker compose -f docker-compose.keycloak.yml exec backend python seed_demo.py

Reads backend/demo_data.json (app catalog with base64 icons + demo houses).
Aborts if apps already exist; pass --force to seed anyway.
"""

import base64
import json
import os
import sys

from database import SessionLocal
from models import App, Asset, User
from crud import get_or_create_platform_type, resolve_platform_types, get_user_by_email

HERE = os.path.dirname(os.path.abspath(__file__))
DATA_FILE = os.path.join(HERE, "demo_data.json")


def main():
    force = "--force" in sys.argv

    with open(DATA_FILE, encoding="utf-8") as f:
        data = json.load(f)

    db = SessionLocal()

    if db.query(App).count() and not force:
        sys.exit("Apps already exist — aborting. Use --force to seed anyway.")

    # 1) platform types (catalog)
    for name in data.get("platform_types", []):
        get_or_create_platform_type(db, name)

    # 2) demo app catalog (icons carried inline as base64)
    for a in data.get("apps", []):
        icon = base64.b64decode(a["icon_b64"]) if a.get("icon_b64") else None
        db.add(App(
            name=a["name"],
            description=a.get("description", ""),
            platform_type=a.get("platform_type", ""),
            price=a.get("price", 0),
            version=a.get("version", ""),
            developer_url=a.get("developer_url", ""),
            install_url=a.get("install_url", ""),
            params_schema=a.get("params_schema", ""),
            icon_data=icon,
            icon_mime=a.get("icon_mime") or None,
        ))
    db.commit()

    # 3) demo houses, attached to the demo user by email. When that user first
    #    signs in via Keycloak, JIT provisioning links this row by email, so the
    #    houses are already theirs.
    email = data.get("demo_user_email")
    houses = data.get("houses", [])
    if email and houses:
        user = get_user_by_email(db, email)
        if user is None:
            user = User(email=email)  # no keycloak_id yet; linked on first login
            db.add(user)
            db.commit()
            db.refresh(user)
        for h in houses:
            asset = Asset(
                name=h["name"],
                address=h.get("address", ""),
                latitude=h.get("latitude"),
                longitude=h.get("longitude"),
                user_id=user.id,
            )
            asset.platform_types = resolve_platform_types(db, h.get("platforms", []))
            db.add(asset)
        db.commit()

    print(f"Seeded {len(data.get('apps', []))} apps and {len(houses)} demo houses "
          f"for {email or '(no demo user)'}.")


if __name__ == "__main__":
    main()
