from app.api.clients.http.dataspace import ConnectorClient
from app.api.models.auth import TokenData
from app.api.services.common import CommonServices


class FileServices(CommonServices):
    async def send(self, dto, token: str) -> dict:
        """
        Simply forwards the JSON to the connector; all validation
        already happened in the Pydantic model.
        """
        async with ConnectorClient(token=token) as client:
            return await client.send_raw_file(dto.model_dump(by_alias=True))