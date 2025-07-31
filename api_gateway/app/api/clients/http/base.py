import httpx
from typing import Optional, Dict, Any
from fastapi import HTTPException
from json import JSONDecodeError

from app.config.logger_setup import logger
from app.utils.helpers import HttpMethods, custom_encoder
from app.api.models.auth import TokenData
from urllib.parse import urljoin


DEFAULT_HEADERS = ['Content-Type', "X-User-ID",
                   "X-User-Type", "X-API-Key", "Authorization"]


class Client:
    def __init__(self,
                 base_url: str,
                 current_user: Optional[TokenData] = None,
                 headers: Optional[Dict[str, str]] = None,
                 api_key: Optional[str] = None,
                 token: Optional[str] = None,
                 ) -> None:

        self.base_url = base_url  # Assign the base_url
        self.headers = headers if headers else {}

        if token:
            if self.headers.get("Authorization") is None:
                self.headers["Authorization"] = f"Bearer {token}"
        if api_key:
            if self.headers.get("X-API-Key") is None:
                self.headers["X-API-Key"] = api_key

        self.client = httpx.AsyncClient()

    def _sanitize_url(self, url: str, endpoint: str) -> str:
        return urljoin(url.rstrip('/') + '/', endpoint.lstrip('/'))

    async def request(
        self,
        method: HttpMethods,
        endpoint: str = '',
        payload: Optional[Dict[str, Any]] = None
    ) -> Any:
        """Builds and sends an HTTP request to the specified endpoint."""
        url = self._sanitize_url(self.base_url, endpoint)  # Ensure proper URL formatting
        logger.info("Preparing and executing HTTP call")
        logger.debug(f"HTTP Call to {url}")
        try:
            # Build the request
            request = self.client.build_request(
                method=method.value,
                url=url,
                json=payload,  # Use 'json' instead of 'data' for JSON requests
                headers=self.headers
            )

            # Send the request and await the response
            response = await self.client.send(request)
            response.raise_for_status()  # Raise error for bad responses (4xx, 5xx)
            logger.info("HTTP call successful")

            # Return JSON if available, otherwise fallback to text
            if 'application/json' in response.headers.get('Content-Type', ''):
                return response.json()
            else:
                return response.text

        except httpx.HTTPStatusError as exc:
            try:
                if exc.response and 'application/json' in exc.response.headers.get('Content-Type', ''):
                    response_details = exc.response.json()
                else:
                    response_details = exc.response.text or "No content returned"
            except (JSONDecodeError, AttributeError):
                response_details = "No valid JSON response"

            logger.error(f"HTTPStatusError - {exc}", exc_info=True)
            raise HTTPException(
                status_code=exc.response.status_code,
                detail=f"Error response {exc.response.status_code} while requesting {url}: {response_details}"
            )

        except httpx.RequestError as exc:
            logger.error(f"RequestError - {exc}", exc_info=True)
            raise HTTPException(
                status_code=500,
                detail=f"Request error: {exc}"
            )

    async def get(self, endpoint: str) -> Any:
        """Helper for GET requests."""
        return await self.request(HttpMethods.GET, endpoint)

    async def post(self, endpoint: str, payload: Optional[Dict[str, Any]] = None) -> Any:
        """Helper for POST requests."""
        encoded = custom_encoder(payload) if payload is not None else None
        return await self.request(HttpMethods.POST, endpoint, encoded)

    async def put(self, endpoint: str, payload: Optional[Dict[str, Any]] = None) -> Any:
        """Helper for PUT requests."""
        encoded = custom_encoder(payload) if payload is not None else None
        return await self.request(HttpMethods.PUT, endpoint, encoded)

    async def delete(self, endpoint: str) -> Any:
        """Helper for DELETE requests."""
        return await self.request(HttpMethods.DELETE, endpoint)

    async def __aenter__(self):
        """Handles the context manager's entry, initializing the client."""
        return self

    async def __aexit__(self, exc_type, exc_val, exc_tb):
        """Handles the context manager's exit, closing the client."""
        await self.client.aclose()