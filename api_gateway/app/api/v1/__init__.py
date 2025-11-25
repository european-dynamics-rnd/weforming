from fastapi import APIRouter
from .routes import logs, backend, dataspace

all_routers = APIRouter()
all_routers.include_router(dataspace.router)
all_routers.include_router(logs.router)
all_routers.include_router(backend.router)
