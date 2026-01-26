#!/bin/bash

# Stop containers
docker-compose -f docker-compose.build.yml down
echo "Backend stopped"

# Start containers
docker-compose -f docker-compose.build.yml up -d --build
echo "Backend started on port 8080"

# Print Swagger UI link
echo ""
echo "Application is ready! Access Swagger UI at: http://localhost:8080/swagger-ui/index.html"
