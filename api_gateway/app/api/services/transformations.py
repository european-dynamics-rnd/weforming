from app.api.models.dtos import (
    MockForecastRequest,
    MockForecastResponse,
    HistoricalDataPoint,
    ForecastEntry
    )
from app.api.models.cdm import (
    Envelope
    )

from app.api.models import (
    ForecastInputEnvelope,
    ForecastOutputEnvelope,
    ForecastOutputMeta,
)

from datetime import datetime, UTC

from app.api.services.common import CommonServices
from app.api.clients.http.forecast_engine import ForecastEngineClient

class ForecastServices(CommonServices):
    @staticmethod
    def _transform_forecast_request(
        env: Envelope
        ) -> MockForecastRequest:
        meta = env.meta
        data = env.data

        if not env.data:
            raise ValueError("Historical data cannot be empty.")
        
        # Extract parameters from meta
        variable    = meta.wibra_variable
        horizon     = meta.wibra_futureHorizon
        granularity = meta.wibra_granularity
        include_ci  = bool(meta.wibra_confInterval)

        # Business call (pure DTOs)
        native_req = MockForecastRequest(
            historical_data=[
                            entry.model_dump() for entry in env.data
                        ],
            variable=variable,
            future_horizon=horizon,
            granularity=granularity,
            confidence_interval=include_ci
        )

        return native_req

    @staticmethod
    def _transform_forecast_response(
        native_resp: MockForecastResponse,
        env: Envelope
        ) -> ForecastOutputMeta:

        # 4. Wrap back into CDM envelope
        resp_meta = ForecastOutputMeta(
            **env.meta.model_dump(by_alias=True),            # copy all wibra:* fields
            wibra_createdAt=datetime.now(UTC)         # bump timestamp
        )
    
        resp_env = ForecastOutputEnvelope(
            meta=resp_meta,
            data=[
                ForecastEntry(
                    timestamp=r.timestamp,
                    predicted_value=r.predicted_value,
                    confidence_interval=r.confidence_interval
                ).model_dump()
                for r in native_resp.forecasts
                
            ]
        )

        

        return resp_env

    async def handle_forecast_request(self, envelope: ForecastInputEnvelope, token: str) -> ForecastOutputEnvelope:

        try:
            transformed_input = self._transform_forecast_request(envelope)
        except Exception as e:
            self.logger.error(f"Input transformation error: {e}")
            raise
        
        try:
            async with ForecastEngineClient(token=token) as client:
                response = await client.get_forecast(transformed_input)
        except Exception as e:
            self.logger.error(f"Forecast engine call failed: {e}")
            raise

        try:
            return self._transform_forecast_response(response, envelope)
        except Exception as e:
            self.logger.error(f"Output transformation error: {e}")
            raise
