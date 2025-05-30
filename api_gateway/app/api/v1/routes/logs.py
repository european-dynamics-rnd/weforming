from fastapi import APIRouter

router = APIRouter(
    prefix='/logs',
    tags=['Logs'],
    )

from fastapi import HTTPException
from aiofiles import open as aio_open
from collections import deque
import json

async def clear_log():
    async with aio_open('logs/w-ibra-api.log', 'w') as log_file:
        await log_file.write('')
    return {"message": "Log cleared successfully"}


async def get_logs(maxlen: int = 20):
    try:
        # Initialize a deque with a maximum length of 20 to keep the last 20 lines
        last_lines = deque(maxlen=maxlen)
        # Open the file asynchronously
        async with aio_open('logs/w-ibra-api.log', 'r') as log_file:
            async for line in log_file:
                # Append each line to the deque
                last_lines.append(line.strip())

        # Process and return the collected lines
        logs = []
        for line in last_lines:
            try:
                # Attempt to parse the line as JSON
                logs.append(json.loads(line))
            except json.JSONDecodeError:
                # Skip lines that are not valid JSON
                continue

        return {"logs": last_lines}

    except FileNotFoundError:
        raise HTTPException(status_code=404, detail="Log file not found.")
    except Exception as e:
        raise HTTPException(
            status_code=500, detail=f"An error occurred: {str(e)}")

router.get('')(get_logs)

router.get('/clear')(clear_log)