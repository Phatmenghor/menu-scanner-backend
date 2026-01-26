#!/bin/bash
# Stop backend only (PostgreSQL keeps running)
docker-compose -f docker-compose.build.yml down
echo "Backend stopped (PostgreSQL still running)"
