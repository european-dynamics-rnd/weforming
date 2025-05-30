from fastapi import FastAPI
from contextlib import asynccontextmanager

from app import __version__


from .api import v1
from .config.logger_setup import logger
from .api.middleware.requests_logging import add_request_id_middleware


DESCRIPTION="""The WeFORMING Gateway API provides a single point
 of access with the W-IBRA dataspace,
   providing all endpoints tha are required for serving or requesting data
"""


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Set a default 'request_id' for startup/shutdown logs
    with logger.contextualize(request_id="startup"):
        # Startup event
        logger.info(f"Starting application version {__version__}")
        yield  # Application runs here
        # Shutdown event
        with logger.contextualize(request_id="shutdown"):
            logger.info("Shutting down application")

app = FastAPI(
    version = __version__,
    description=DESCRIPTION,
    title="WeFORMING Gateway API",
    lifespan=lifespan)

# Add the logging middleware
app.middleware("http")(add_request_id_middleware)
app.include_router(v1.all_routers)
