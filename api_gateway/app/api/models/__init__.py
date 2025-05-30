from app.api.models.cdm import (
    Envelope,
    ForecastInputMeta,
    HistoricalDataPoint,
    ForecastOutputMeta,
    ForecastEntry    
)
from app.api.models.dtos import (
    MockForecastResponse,
    MockForecastResponse,
)

from typing import List

# Define concrete envelope types
ForecastInputEnvelope  = Envelope[ForecastInputMeta,  List[HistoricalDataPoint]]
ForecastOutputEnvelope = Envelope[ForecastOutputMeta, List[ForecastEntry]]