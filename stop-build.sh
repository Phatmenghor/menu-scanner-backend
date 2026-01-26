#!/bin/bash
# Stop backend and PostgreSQL (from build compose)
docker-compose -f docker-compose.build.yml down
echo "Backend and PostgreSQL stopped"
