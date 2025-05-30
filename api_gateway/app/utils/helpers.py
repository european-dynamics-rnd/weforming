
import enum
import datetime

class HttpMethods(enum.Enum):
    GET = "GET"
    POST = "POST"
    PUT = "PUT"
    DELETE = "DELETE"

class ForecastingVariables(enum.Enum):
    DEMAND = "DEMAND"
    GENERATION = "GENERATION"

class TimeWindow(str, enum.Enum):
    ONE_HOUR = '1H'
    SIX_HOURS = '6H'
    TWELVE_HOURS = '12H'
    TWENTY_FOUR_HOURS = '24H'
    FORTY_EIGHT_HOURS = '48H'
    SEVENTY_TWO_HOURS = '72H'

def custom_encoder(data):
    if isinstance(data, enum.Enum):
        return data.value  # Convert Enum to its value
    elif isinstance(data, datetime.datetime):
        return data.isoformat()
    elif isinstance(data, datetime.date):
        return data.isoformat()
    elif isinstance(data, dict):
        return {key: custom_encoder(value) for key, value in data.items()}
    elif isinstance(data, list):
        return [custom_encoder(item) for item in data]
    else:
        return data
    

