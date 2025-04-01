# Start Docker Compose services
containers-start:
	@echo "Bringing up Docker Compose services with build..."
	docker compose -f docker-compose_Postgres.yml up --build -d

# Restart Docker Compose services without removing volumes
containers-restart:
	@echo "Restarting Docker Compose services..."
	docker compose -f docker-compose_Postgres.yml down
	$(MAKE) containers-start

# Ensure Makefile uses tabs for indentation
.PHONY: containers-start containers-restart
