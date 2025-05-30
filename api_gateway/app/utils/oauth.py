from fastapi import Header, HTTPException, Depends
from app.api.services.auth import AuthServices

async def get_dataspace_token(
    dataspace_user: str = Header(..., alias="X-Dataspace-User"),
    auth_service: AuthServices = Depends(),
) -> str:
    """
    Dependency that reads the dataspace username from the X-Dataspace-User header
    and returns a fresh access token (or 401 if none is cached).
    """
    try:
        return await auth_service.get_access_token(dataspace_user)
    except HTTPException as e:
        # Bubble up 401 so FastAPI returns it directly
        raise e