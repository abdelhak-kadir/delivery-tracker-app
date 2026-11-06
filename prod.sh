#!/bin/bash

set -e

echo "Stopping any running containers..."
sudo docker compose -f docker/docker-compose.prod.yaml --env-file .env down

echo "Building and starting containers..."
sudo docker compose -f docker/docker-compose.prod.yaml --env-file .env up --build
