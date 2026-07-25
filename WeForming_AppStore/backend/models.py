from sqlalchemy import Boolean, Column, ForeignKey, Integer, String, LargeBinary, JSON, Float, Table
from sqlalchemy.orm import relationship
from database import Base

# Many-to-many: which platform types an asset (house) supports.
asset_platforms = Table(
    "asset_platforms",
    Base.metadata,
    Column("asset_id", ForeignKey("assets.id", ondelete="CASCADE"), primary_key=True),
    Column("platform_type_id", ForeignKey("platform_types.id", ondelete="CASCADE"), primary_key=True),
)


class PlatformType(Base):
    """Catalog of installation platform options (e.g. Docker, Web). Extensible."""
    __tablename__ = "platform_types"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, unique=True, index=True)


class User(Base):
    __tablename__ = "users"
    id = Column(Integer, primary_key=True, index=True)
    # Keycloak subject (sub) — the stable identifier we key identities on.
    keycloak_id = Column(String, unique=True, index=True, nullable=True)
    email = Column(String, unique=True, index=True)
    hashed_password = Column(String, nullable=True)  # legacy; unused with Keycloak
    is_active = Column(Boolean, default=True)

class Asset(Base):
    __tablename__ = "assets"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String)           # building name, e.g. "Summer House"
    address = Column(String, default="")
    latitude = Column(Float)
    longitude = Column(Float)
    image_data = Column(LargeBinary)  # building photo stored in the DB
    image_mime = Column(String)
    user_id = Column(Integer, ForeignKey("users.id"), index=True)
    # (a legacy JSON `platforms` column may remain in the DB; it is unused now)

    platform_types = relationship("PlatformType", secondary=asset_platforms, lazy="selectin")

    @property
    def platforms(self):
        """Supported platform names, e.g. ["Docker", "Web"]."""
        return [pt.name for pt in self.platform_types]

    @property
    def image_url(self):
        return f"/api/assets/{self.id}/image" if self.image_data else None

class UserApp(Base):
    __tablename__ = "user_apps"
    id = Column(Integer, primary_key=True, index=True)
    app_id = Column(Integer, ForeignKey("apps.id"))
    user_id = Column(Integer, ForeignKey("users.id"), index=True)
    asset_id = Column(Integer, ForeignKey("assets.id"))
    json_config = Column(String, default="{}")
    installed_version = Column(String, default="")
    # Async install tracking: unique id + secret sent to the installer; the installer
    # calls back (with the secret) to flip status from "in_progress" to "finished".
    install_uuid = Column(String, unique=True, index=True)
    secret_key = Column(String)
    status = Column(String, default="in_progress")

    app = relationship("App")
    asset = relationship("Asset")

class App(Base):
    __tablename__ = "apps"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, index=True)
    description = Column(String)
    icon = Column(String)  # legacy: external URL/path (kept for backward compat)
    icon_data = Column(LargeBinary)  # icon bytes stored in the DB
    icon_mime = Column(String)       # e.g. "image/png"
    rating = Column(Integer, default=0)
    price = Column(Integer, default=0)
    dockerID = Column(String, default="")
    platform_type = Column(String, default="")
    developer_id = Column(Integer, ForeignKey("users.id"))  # the developer who published it
    developer_url = Column(String, default="")   # developer-provided link (website/profile)
    version = Column(String, default="")         # app version, e.g. "1.0.0"
    install_url = Column(String, default="")     # hook called during install with the params
    params_schema = Column(String, default="")   # JSON Forms schema for install-time parameters

    developer = relationship("User")

    @property
    def icon_url(self):
        """Relative URL the frontend uses to fetch the icon, or None."""
        if self.icon_data:
            return f"/api/apps/{self.id}/icon"
        return self.icon or None

    @property
    def developer_email(self):
        """Email of the publishing developer — a mailto link target, or None."""
        return self.developer.email if self.developer else None
    