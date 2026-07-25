from pydantic import BaseModel
from typing import List, Optional


class Token(BaseModel):
    access_token: str
    token_type: str

class Me(BaseModel):
    """Current authenticated user, as classified by Keycloak client roles."""
    email: Optional[str] = None
    username: Optional[str] = None
    roles: List[str] = []
    is_developer: bool = False


class PlatformType(BaseModel):
    id: int
    name: str
    class Config:
        from_attributes = True

class PlatformTypeCreate(BaseModel):
    name: str

class Asset(BaseModel):
    id: int
    user_id: int
    name: str
    address: Optional[str] = None
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    platforms: List[str] = []          # supported platform names
    image_url: Optional[str] = None
    class Config:
        from_attributes = True


class AppBase(BaseModel):
    name: str
    description: Optional[str] = None
    rating: int = 0
    price: int = 0
    platform_type: Optional[str] = None
    developer_url: Optional[str] = None
    version: Optional[str] = None
    install_url: Optional[str] = None
    params_schema: Optional[str] = None  # JSON Forms schema (as a JSON string)

class AppCreate(AppBase):
    pass

class App(AppBase):
    id: int
    icon_url: Optional[str] = None  # served from the DB via /api/apps/{id}/icon
    developer_email: Optional[str] = None  # publishing developer (mailto target)
    class Config:
        from_attributes = True


class UserAppCreate(BaseModel):
    app_id: int
    asset_id: int
    json_config: Optional[str] = "{}"
    installed_version: Optional[str] = ""

class UserAppDetail(BaseModel):
    id: int
    user_id: int
    app_id: int
    asset_id: int
    json_config: Optional[str] = None
    installed_version: Optional[str] = None
    install_uuid: Optional[str] = None
    status: Optional[str] = None  # in_progress | finished
    app: App
    asset: Asset
    class Config:
        from_attributes = True


class InstallStatus(BaseModel):
    install_uuid: str
    status: str


class InstallConfirm(BaseModel):
    install_uuid: str
    secret_key: str
    success: bool = True  # default success; set false to mark the install failed
