from sqlalchemy.orm import Session
from models import App as DBApp, Asset as DBAsset, User, UserApp as DBUserApp, PlatformType
from schemas import AppCreate, UserAppCreate

def get_user(db: Session, user_id: int):
    return db.query(User).filter(User.id == user_id).first()

def get_user_by_email(db: Session, email: str):
    return db.query(User).filter(User.email == email).first()

def get_or_create_keycloak_user(db: Session, keycloak_id: str, email: str = None):
    """Map a Keycloak identity to a local users row, creating it on first sight.

    Lookup order: by keycloak_id, then by email (backfilling keycloak_id so
    pre-existing rows get linked), otherwise create a fresh row. New users start
    with no houses — they add their own in the asset editor.
    """
    user = db.query(User).filter(User.keycloak_id == keycloak_id).first()
    if user:
        return user

    if email:
        user = get_user_by_email(db, email)
        if user:
            user.keycloak_id = keycloak_id
            db.commit()
            db.refresh(user)
            return user

    user = User(keycloak_id=keycloak_id, email=email)
    db.add(user)
    db.commit()
    db.refresh(user)
    return user

# --- Platform types (catalog) ------------------------------------------------

def get_platform_types(db: Session):
    return db.query(PlatformType).order_by(PlatformType.name).all()

def get_or_create_platform_type(db: Session, name: str):
    pt = db.query(PlatformType).filter(PlatformType.name == name).first()
    if not pt:
        pt = PlatformType(name=name)
        db.add(pt)
        db.commit()
        db.refresh(pt)
    return pt

def resolve_platform_types(db: Session, names):
    """Existing PlatformType rows for the given names (unknown names are ignored)."""
    if not names:
        return []
    return db.query(PlatformType).filter(PlatformType.name.in_(list(names))).all()

# --- Assets (houses) ---------------------------------------------------------

def get_assets_by_user(db: Session, user_id: int):
    return db.query(DBAsset).filter(DBAsset.user_id == user_id).all()

def get_asset(db: Session, asset_id: int):
    return db.query(DBAsset).filter(DBAsset.id == asset_id).first()

def create_asset(db: Session, user_id: int, name: str, address: str = "",
                 latitude: float = None, longitude: float = None,
                 platform_names=None, image_data: bytes = None, image_mime: str = None):
    asset = DBAsset(
        name=name, address=address, latitude=latitude, longitude=longitude,
        image_data=image_data, image_mime=image_mime, user_id=user_id,
    )
    asset.platform_types = resolve_platform_types(db, platform_names or [])
    db.add(asset)
    db.commit()
    db.refresh(asset)
    return asset

def delete_asset(db: Session, asset):
    db.delete(asset)
    db.commit()

def get_apps(db: Session, skip: int = 0, limit: int = 100):
    return db.query(DBApp).offset(skip).limit(limit).all()

def get_app(db: Session, app_id: int):
    return db.query(DBApp).filter(DBApp.id == app_id).first()

def create_app(db: Session, app: AppCreate, icon_data: bytes = None, icon_mime: str = None, developer_id: int = None):
    db_app = DBApp(**app.model_dump(), icon_data=icon_data, icon_mime=icon_mime, developer_id=developer_id)
    db.add(db_app)
    db.commit()
    db.refresh(db_app)
    return db_app


def search_apps(db: Session, query: str):
    return db.query(DBApp).filter(DBApp.name.contains(query)).all()

def get_user_apps(db: Session, user_id: int):
    return db.query(DBUserApp).filter(DBUserApp.user_id == user_id).all()

def get_user_app_by_uuid(db: Session, install_uuid: str):
    return db.query(DBUserApp).filter(DBUserApp.install_uuid == install_uuid).first()

def create_user_app(db: Session, user_id: int, user_app: UserAppCreate,
                    json_config: str = None, install_uuid: str = None,
                    secret_key: str = None, status: str = "in_progress"):
    data = user_app.model_dump()
    if json_config is not None:
        data["json_config"] = json_config
    db_user_app = DBUserApp(
        **data, user_id=user_id,
        install_uuid=install_uuid, secret_key=secret_key, status=status,
    )
    db.add(db_user_app)
    db.commit()
    db.refresh(db_user_app)
    return db_user_app
