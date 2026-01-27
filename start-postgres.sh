#!/bin/bash

# Stop containers
docker-compose -f docker-compose.window.yml down
echo "PostgreSQL stopped"

# Start containers
docker-compose -f docker-compose.window.yml up -d
echo "PostgreSQL started on port 1111"
