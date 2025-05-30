import pytest
from datetime import datetime
from app.api.services.transformations import ForecastServices
from app.api.models import (
    ForecastInputMeta,
    HistoricalDataPoint,
    ForecastEntry,
    ForecastInputEnvelope,
    MockForecastResponse
)
from app.utils.helpers import ForecastingVariables, TimeWindow
from pydantic import ValidationError

@pytest.fixture

def valid_input_envelope():
    meta = ForecastInputMeta(
        wibra_schema="file:///schemas/ForecastInput-v1.json",
        wibra_type="array",
        wibra_itemSchema="file:///schemas/HistoricalDataPoint-v1.json",
        wibra_contentType="application/json",
        wibra_unit="unit:WATT",
        wibra_service="ForecastService",
        wibra_createdAt=datetime(2025, 5, 15, 12, 0, 0),
        wibra_location="demo1",
        wibra_license="https://creativecommons.org/licenses/by/4.0/",
        wibra_variable=ForecastingVariables.DEMAND,
        wibra_futureHorizon=TimeWindow.TWENTY_FOUR_HOURS,
        wibra_granularity="PT15M",
        wibra_confInterval=True
    )
    data = [
        HistoricalDataPoint(timestamp=datetime(2025, 5, 14, 12, 0, 0), value=21.4),
        HistoricalDataPoint(timestamp=datetime(2025, 5, 14, 12, 15, 0), value=21.6),
    ]
    return ForecastInputEnvelope(meta=meta, data=data)


def test_transform_forecast_request_valid(valid_input_envelope):
    req = ForecastServices._transform_forecast_request(valid_input_envelope)
    assert req.variable == ForecastingVariables.DEMAND
    assert req.future_horizon == TimeWindow.TWENTY_FOUR_HOURS.value
    assert len(req.historical_data) == 2


def test_transform_forecast_request_empty_data():
    meta = ForecastInputMeta(
        wibra_schema="file:///schemas/ForecastInput-v1.json",
        wibra_type="array",
        wibra_itemSchema="file:///schemas/HistoricalDataPoint-v1.json",
        wibra_contentType="application/json",
        wibra_unit="unit:WATT",
        wibra_service="ForecastService",
        wibra_createdAt=datetime.now(),
        wibra_location="loc",
        wibra_license="lic",
        wibra_variable=ForecastingVariables.DEMAND,
        wibra_futureHorizon=TimeWindow.ONE_HOUR,
        wibra_granularity="PT15M",
        wibra_confInterval=None
    )
    env = ForecastInputEnvelope(meta=meta, data=[])
    with pytest.raises(ValueError):
        ForecastServices._transform_forecast_request(env)


def test_transform_forecast_response_valid(valid_input_envelope):
    
    class DummyResponse:
        timestamps = [datetime(2025, 5, 15, 12, 15, 0), datetime(2025, 5, 15, 12, 30, 0)]
        values = [22.8, 22.9]
        confidence_interval = [[22.0, 23.6], [22.1, 23.7]]
        future_horizon = TimeWindow.TWENTY_FOUR_HOURS

    resp = MockForecastResponse(
        forecasts=[
            ForecastEntry(timestamp=ts, predicted_value=val, confidence_interval=ci).model_dump()
            for ts, val, ci in zip(DummyResponse.timestamps, DummyResponse.values, DummyResponse.confidence_interval)
        ],
        variable=valid_input_envelope.meta.wibra_variable,
        future_horizon=valid_input_envelope.meta.wibra_futureHorizon,
        granularity="PT15M"
    )
    output_env = ForecastServices._transform_forecast_response(resp, valid_input_envelope)
    assert output_env.meta.wibra_futureHorizon == TimeWindow.TWENTY_FOUR_HOURS
    assert len(output_env.data) == 2
    assert output_env.data[0].predicted_value == 22.8
