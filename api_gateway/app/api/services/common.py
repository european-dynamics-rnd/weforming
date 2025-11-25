from datetime import datetime, timezone, timedelta
import random
from app.config.logger_setup import logger

class CommonServices:
    def __init__(self):
        self.logger = logger

    @staticmethod
    async def get_timestamp():
        now = datetime.now(timezone.utc)   
        return [(now+timedelta(minutes=i*5)).isoformat() for i in range(1, 12*3)]

    @staticmethod
    async def get_list_of_random_floats(length):
        return [random.random() for _ in range(length)]
    
