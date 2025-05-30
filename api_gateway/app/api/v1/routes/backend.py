from fastapi import APIRouter, Depends, Body
from app.utils.oauth import get_dataspace_token
from app.api.services.transformations import ForecastServices
from app.api.models import (
    ForecastInputEnvelope,
    ForecastOutputEnvelope,
)
router = APIRouter(prefix="/backend", tags=["Backend"])


@router.post("/forecasts", response_model=ForecastOutputEnvelope)
async def forecasts(
    envelope: ForecastInputEnvelope = Body(..., media_type="application/ld+json"),
    ctoken: str = Depends(get_dataspace_token),
    srv: ForecastServices = Depends(),
):
    return await srv.handle_forecast_request(envelope, ctoken)
