#!/bin/bash

# Stop containers
docker-compose -f docker-compose.window.yml down
echo "PostgreSQL stopped"
