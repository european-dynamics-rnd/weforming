from pydantic import BaseModel, Field
from app.utils.helpers import ForecastingVariables, TimeWindow
from datetime import datetime
from typing import List, Optional

class HistoricalDataPoint(BaseModel):
    timestamp: datetime = Field("Timestamp of historical data point in ISO format")
    value: float = Field("The value of the historical data point")

class MockForecastRequest(BaseModel):
    historical_data: List[HistoricalDataPoint] = Field(description="A list of historical data")
    variable: ForecastingVariables = Field(description="The type of variable to be forecasted")
    future_horizon: TimeWindow = Field(description="The future horizon, how far in the future the forecast should be.")

class ForecastEntry(BaseModel):
    timestamp: datetime
    predicted_value: float
    confidence_interval: Optional[List[float]] = None

class MockForecastResponse(BaseModel):
    forecasts: List[ForecastEntry] = Field("List of forecast entries")
    variable: ForecastingVariables = Field(description="The type of variable to be forecasted")
    future_horizon: TimeWindow = Field(description="The future horizon, how far in the future the forecast should be.")
    granularity:str=Field(description="The granularity of the predictions")