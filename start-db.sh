#!/bin/bash
# Start PostgreSQL only for local development
docker-compose up -d
echo "PostgreSQL started on port 5432"
