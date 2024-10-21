# Define the target for refreshing containers
containers-reset:
	@echo "Bringing down Docker Compose services..."
	docker compose down
	@echo "Removing Docker volumes..."
	docker volume rm ed-edc_edc_postgres_volume || true
	@echo "Bringing up Docker Compose services with build..."
	docker compose up --build -d

# Ensure Makefile uses tabs for indentation
.PHONY: containers-reset
