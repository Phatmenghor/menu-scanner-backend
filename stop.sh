#!/bin/bash
# Stop all containers
docker-compose down
docker-compose -f docker-compose.build.yml down
echo "All containers stopped"
