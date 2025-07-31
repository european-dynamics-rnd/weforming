from app.api.clients.http.base import Client
from app.config.environment_variables import settings as s

class MiddlewareClient(Client):
    """
    MiddlewareClient class to handle HTTP requests to the dataspace API.
    Inherits from the base Client class for HTTP requests.
    """
    def __init__(self, headers: dict = None, token:str = None) -> None:
        super().__init__(
            base_url=s.middleware_url, 
            token=token,
            headers=headers
            )

    async def login(self, username: str, password: str) -> dict:
        """
        Perform login to the IAM service of the dataspace and obtain tokens.
        """
        data = {
            "username": username,
            "password": password,
        }

        return await self.post(
            "/api/user/auth/token",
            payload=data,
        )
    
class ConnectorClient(Client):
    def __init__(self, headers: dict = None, token:str = None) -> None:
        super().__init__(
            base_url=s.middleware_url, 
            token=token,
            headers=headers
            )
        
    async def send_raw_file(self, payload: dict) -> dict:
        """
        POST the raw JSON payload to the EDC connector.
        The endpoint/path and headers follow the legacy script exactly.
        """
        return await self.post(
            "/handler/asset/provide",  
            #"/api/dataset/provide-data",               # <-- adjust to real path
            payload=payload
        )