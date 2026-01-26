#!/bin/bash
docker-compose -f docker-compose.build.yml up -d --build
echo "Backend started on port 8080"
