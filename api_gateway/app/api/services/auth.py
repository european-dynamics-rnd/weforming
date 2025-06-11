from fastapi import HTTPException
from app.api.services.common import CommonServices
from app.api.clients.http.dataspace import MiddlewareClient
from app.config.logger_setup import logger

# In-memory cache for refresh tokens (replace with Redis or vault in prod)
token_cache = {}

class AuthServices(CommonServices):
    """
    AuthServices class to handle authentication-related operations.
    Inherits from the base Client class for HTTP requests.
    """
        
    async def dataspace_login(self, username: str, password: str) -> dict:
        """
        Perform login to the IAM service of the dataspace and obtain tokens.
        """
        
        logger.info(f"Logging in with username: {username}")
        headers = {
            "Content-Type": "application/json"
        }
        async with MiddlewareClient(headers=headers) as client:
            # Perform login to the IAM service
            token_data = await client.login(
                username=username,
                password=password
            )
        
        token_cache[username] = {
            "access": token_data.get("accessToken"),
            "refresh": token_data.get("refreshToken"),
        }

        return token_data
    
    async def get_access_token(self, username: str) -> str:
        """
        Obtain a fresh access token using a stored refresh token.
        """

        
        tokens = token_cache.get(username)

        if not tokens:
            logger.error(f"User {username} not found in token cache.")
            raise HTTPException(
                status_code=401,
                detail="User not found; please log in again."
            )

        access_token = tokens.get("access")
        
        if not access_token:
            logger.error("No access token found in cache.")
            raise HTTPException(
                status_code=401,
                detail="No access token found; please log in again."
            )

        return access_token