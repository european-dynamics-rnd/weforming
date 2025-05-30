from pydantic_settings import BaseSettings, SettingsConfigDict
from typing import ClassVar

class Settings(BaseSettings):
    middleware_url: str
    connector_url: str
    uvicorn_host: str
    uvicorn_port: str
    uvicorn_workers: str

    model_config: ClassVar[dict] = SettingsConfigDict(
        env_file='ed.env'
    )

settings = Settings()