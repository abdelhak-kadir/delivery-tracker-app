#!/bin/bash

# presist the volume for the database
# docker compose -f docker/docker-compose.local.yaml --env-file .env down 

docker compose -f docker/docker-compose.local.yaml --env-file .env down -v
