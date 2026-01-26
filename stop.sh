#!/bin/bash
# Stop all containers
docker-compose down 2>/dev/null
docker-compose -f docker-compose.build.yml down 2>/dev/null
echo "All containers stopped (including PostgreSQL)"
