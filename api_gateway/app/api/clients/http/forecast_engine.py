from app.api.clients.http.base import Client
from app.api.models.dtos import MockForecastResponse
URL = "http://localhost:8000"  # Replace with actual URL if needed
NotImplemented = True  # Set to True for mocking purposes

class ForecastEngineClient(Client):
    """
    ForecastEngineClient class to handle HTTP requests to the forecast engine API.
    Inherits from the base Client class for HTTP requests.
    """
    def __init__(self, headers: dict = None, token:str = None) -> None:
        super().__init__(
            base_url=URL, 
            token=token,
            headers=headers
            )
        
    async def get_forecast(self, payload: dict) -> dict:
        """
        POST the JSON payload to the forecast engine.
        The endpoint/path and headers follow the legacy script exactly.
        """
        if NotImplemented:
            return {
                "forecasts": [
                    {
                    "timestamp": "2025-05-14T12:15:00Z",
                    "predicted_value": 22.8,
                    "confidence_interval": [22.0, 23.6]
                    },
                    {
                    "timestamp": "2025-05-14T12:30:00Z",
                    "predicted_value": 22.9,
                    "confidence_interval": [22.1, 23.7]
                    }
                ],
                "variable": "DEMAND",
                "future_horizon": "1H",
                "granularity": "PT15M"
                }
        
        return await self.post(
            "/api/forecast",  # Adjust to the actual endpoint
            payload=payload
        )