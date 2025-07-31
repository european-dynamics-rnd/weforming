import contextvars
from loguru import logger
import sys

# Create a context variable to store the request ID (UUID)
request_id_context = contextvars.ContextVar("request_id", default=None)

# Define a log format that includes the request ID (UUID)
log_format = "<green>{time}</green> | <level>{level}</level> | <cyan>{name}</cyan>:<cyan>{function}</cyan> | {message} | <yellow>{extra[request_id]}</yellow>"

# Configure Loguru to use the format
logger.configure(handlers=[{"sink": sys.stdout, "format": log_format}],
                 extra={"request_id": "n/a"})

# Add file logging with rotation and retention, also including the UUID
logger.add("logs/w-ibra-api.log", format=log_format, rotation="1 week",
           retention="1 month", level="DEBUG")




















