# 🔌 WeFormingConnector – API Gateway

The **WeForming API gateway** serves as the main entry point and data transformation layer between internal microservices and external data consumers/providers. It supports data exchange, canonical model validation, and backend orchestration.

---

## 🚀 Features

- 🌍 Dataspace-compatible APIs for data service ingestion, authentication, and transformation.
- 🔐 OAuth2-ready authentication
- 🧠 Canonical data transformation services
- 📊 HTML test reporting with `pytest-html`
- 🧾 Structured logging with request-scoped UUIDs
- ⚙️ Service-oriented architecture (SOA)

---

## 🧱 Project Structure

```
api_gateway/
├── app/
│   ├── api/              # Route definitions
│   ├── models/           # Pydantic schemas
│   ├── services/         # Forecast/Auth service logic
│   ├── clients/          # HTTP clients to external services
│   ├── config/           # Logging and settings
│   └── utils/            # Helpers and constants
├── tests/                # Unit tests
├── logs/                 # Rotating logs
└── ReadMe.md             # You are here
```

---

## 📦 Setup Instructions

### 🐍 Python

- Python 3.11+
- Recommended: `venv` or `poetry`

```bash
cd api_gateway
python -m venv venv
source venv/bin/activate
pip install -r requirements.txt
```

### 🔐 .env File

Create a `.env` file with:

```.env
MIDDLEWARE_URL = https://example.com/
CONNECTOR_URL = https://example-connector.com/
UVICORN_HOST = 0.0.0.0
UVICORN_PORT = 8000
UVICORN_WORKERS = 2
```

Update the configuration of the [Settings](/api_gateway/app/config/environment_variables.py) class to point to the .env file.

---

## 🧪 Running Tests

```bash
pytest
```

This runs all unit tests and:

- Generates an HTML report at `report.html`
- Automatically renames it with a timestamp and opens it in your browser
- Stores reports in `tests/reports/`

---

## 📝 Logging

Loguru-based logging with UUID support:

- Console and rotating file logs: `logs/w-ibra-api.log`
- Format includes timestamps, log level, function, and request_id
- Request ID is injected into each log via `contextvars` (FastAPI middleware)

---

## 📚 Illustrative Services

### ForecastServices

- `_transform_forecast_request(env)`  
  → Transforms Canonical Envelope input to `MockForecastRequest`

- `_transform_forecast_response(response, env)`  
  → Wraps native output into Canonical `ForecastOutputMeta`

### AuthServices

- `dataspace_login(username, password)`  
  → Logs into dataspace IAM and caches token

- `get_access_token(username)`  
  → Returns access token from memory

---

## 🧰 Developer Notes

- `conftest.py` handles test report renaming and browser launch
- Test config is in `pytest.ini`
- HTML report format: `report_<TIMESTAMP>.html`

---
