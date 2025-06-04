from pydantic import BaseModel, Field, ConfigDict
from datetime import datetime
from typing import List, ClassVar, Generic, TypeVar, Optional
from uuid import uuid4
from app.utils.helpers import TimeWindow, ForecastingVariables


TMeta = TypeVar("TMeta")
TData = TypeVar("TData")

# --- WibraEnvelopeMeta class ---
class WibraEnvelopeMeta(BaseModel):
    """All envelopes share this minimal set of wibra:* descriptors."""
    wibra_schema:     str          = Field(..., alias="wibra:schema")
    wibra_type:       str          = Field(..., alias="wibra:type")
    wibra_itemSchema: str          = Field(..., alias="wibra:itemSchema")
    wibra_contentType: Optional[str] = Field(None, alias="wibra:contentType")
    wibra_unit:       Optional[str] = Field(None, alias="wibra:unit")
    wibra_service:    Optional[str] = Field(None, alias="wibra:service")
    wibra_createdAt:  datetime     = Field(..., alias="wibra:createdAt")
    wibra_location:   Optional[str] = Field(None, alias="wibra:location")
    wibra_license:    Optional[str] = Field(None, alias="wibra:license")

    model_config: ClassVar[dict] = ConfigDict(
        populate_by_name=True
    )

class Envelope(BaseModel, Generic[TMeta, TData]):
    # JSON-LD header
    context: dict = Field({
        "@vocab": "https://w3id.org/edc/v0.0.1/ns/",
        "wibra":  "https://w3id.org/your-org/wibra#"
    }, alias="@context")
    edctype: str = Field("dataspaceconnector:envelope")
    id:       str = Field(default_factory=lambda: f"urn:uuid:{uuid4()}", alias="@id")

    # payload
    meta: TMeta
    data: TData

    model_config: ClassVar[dict] = ConfigDict(
        populate_by_name=True
    )


class ForecastInputMeta(WibraEnvelopeMeta):
    """Forecast service parameters, all in wibra: namespace."""
    wibra_variable:       ForecastingVariables = Field(..., alias="wibra:variable")
    wibra_futureHorizon:  TimeWindow           = Field(..., alias="wibra:futureHorizon")
    wibra_granularity:    str                  = Field(..., alias="wibra:granularity")
    wibra_confInterval:   Optional[bool]       = Field(None, alias="wibra:confidenceInterval")

class HistoricalDataPoint(BaseModel):
    timestamp: datetime
    value:     float

class ForecastOutputMeta(WibraEnvelopeMeta):
    """Echoes input params plus nothing new for this service."""
    wibra_variable:       ForecastingVariables = Field(..., alias="wibra:variable")
    wibra_futureHorizon:  TimeWindow           = Field(..., alias="wibra:futureHorizon")
    wibra_granularity:    str                  = Field(..., alias="wibra:granularity")
    wibra_confInterval:   Optional[bool]       = Field(None, alias="wibra:confidenceInterval")

class ForecastEntry(BaseModel):
    timestamp:           datetime
    predicted_value:     float
    confidence_interval: Optional[List[float]] = None

