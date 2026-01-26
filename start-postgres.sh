#!/bin/bash

# Stop containers
docker-compose down
echo "PostgreSQL stopped"

# Start containers
docker-compose up -d
echo "PostgreSQL started on port 5432"

