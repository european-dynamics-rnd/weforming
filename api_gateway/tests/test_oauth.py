import pytest
from unittest.mock import AsyncMock, patch
from app.api.services.auth import AuthServices

@pytest.mark.asyncio
async def test_dataspace_login():

    # Fake token response
    fake_token_data = {
        "accessToken": "fake-access-token",
        "refreshToken": "fake-refresh-token"
    }

    # Patch MiddlewareClient context manager
    with patch("app.api.services.auth.MiddlewareClient") as MockClient:      
        mock_client_instance = AsyncMock()
        mock_client_instance.login.return_value = fake_token_data
        MockClient.return_value.__aenter__.return_value = mock_client_instance

        result = await AuthServices().dataspace_login("testuser", "testpass")

        assert result["accessToken"] == "fake-access-token"
        assert result["refreshToken"] == "fake-refresh-token"