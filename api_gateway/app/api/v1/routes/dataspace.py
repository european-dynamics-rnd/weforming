from fastapi import APIRouter, Depends
from app.api.services.auth import AuthServices
from app.api.models.auth import TokenRequest, Tokens

router = APIRouter(prefix='/dataspace', tags=["Dataspace"])


@router.post("/auth", response_model=Tokens)
async def login(request: TokenRequest, auth_services: AuthServices = Depends()):
    """
    Initial login: exchanges username/password for tokens.
    """
    return await auth_services.dataspace_login(request.username, request.password)