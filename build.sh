#!/bin/bash
# Build and start backend with PostgreSQL for frontend to call
docker-compose -f docker-compose.build.yml up -d --build
echo "Backend started on port 8080"
echo "PostgreSQL started on port 5432"
