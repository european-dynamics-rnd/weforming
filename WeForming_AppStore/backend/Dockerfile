FROM python:3.11-slim

WORKDIR /app
RUN mkdir -p static/icons
COPY static/icons/* ./static/icons
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
