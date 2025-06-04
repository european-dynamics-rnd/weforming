import uuid
import time
from fastapi import Request
from app.config.logger_setup import logger, request_id_context

# Middleware to add a unique request ID (UUID) to each incoming request
async def add_request_id_middleware(request: Request, call_next):
    # Generate a UUID for the request
    request_id = str(uuid.uuid4())

    # Set the request_id in the context variable
    request_id_context.set(request_id)

    # Add the request_id to the Loguru "extra" context
    with logger.contextualize(request_id=request_id):
        info = f"New request. Client {request.client}, method {request.method}, path {request.url.path}"
        if request.url.query:
            info += f", query {request.url.query}"
        logger.info(info)
        start_time = time.time()
        response = await call_next(request)
        process_time = time.time() - start_time

        logger.info(
            f"Request completed. status_code {response.status_code}, process_time={process_time:.2f}s")
        # Include the request ID in the response headers (optional)
        response.headers['X-Request-ID'] = request_id

    return response

