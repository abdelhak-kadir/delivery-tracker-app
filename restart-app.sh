SERVICE_NAME="app" # The name of your Spring Boot service in docker-compose.local.yaml
#!/bin/bash

# Exit immediately if a command exits with a non-zero status.
set -e

# Define the path to your Docker Compose file and environment file
DOCKER_COMPOSE_FILE="docker/docker-compose.local.yaml"
ENV_FILE=".env"
SERVICE_NAME="app" # The name of your Spring Boot service in docker-compose.local.yaml

echo "Stopping and removing the '$SERVICE_NAME' container..."
# Stop and remove the existing 'app' container
# -f: Force removal of containers (don't ask for confirmation)
docker compose -f "$DOCKER_COMPOSE_FILE" --env-file "$ENV_FILE" stop "$SERVICE_NAME"
docker compose -f "$DOCKER_COMPOSE_FILE" --env-file "$ENV_FILE" rm -f "$SERVICE_NAME"

echo "Building the '$SERVICE_NAME' service image..."
# Rebuild the 'app' service image to pick up any code changes
docker compose -f "$DOCKER_COMPOSE_FILE" --env-file "$ENV_FILE" build "$SERVICE_NAME"

echo "Starting the '$SERVICE_NAME' container..."
# Start only the 'app' service.
# --no-deps: Prevents Docker Compose from starting linked services (like 'db' or 'front')
#            if they are already running or not explicitly stopped/removed.
# -d: Run container in detached mode (in the background)
docker compose -f "$DOCKER_COMPOSE_FILE" --env-file "$ENV_FILE" up -d "$SERVICE_NAME" --no-deps

echo "Spring Boot app container rebuilt and restarted successfully!"
