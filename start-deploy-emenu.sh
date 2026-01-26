#!/bin/bash

COMPOSE_FILE=docker-compose.build.yml
CONTAINER_NAME=emenu_backend

# Stop and remove old container if exists
if [ "$(docker ps -aq -f name=$CONTAINER_NAME)" ]; then
    echo "Stopping old container..."
    docker stop $CONTAINER_NAME
    echo "Removing old container..."
    docker rm $CONTAINER_NAME
fi

# Build and start new container
echo "Building and starting backend using $COMPOSE_FILE..."
docker-compose -f $COMPOSE_FILE build backend
docker-compose -f $COMPOSE_FILE up -d backend

echo "Backend started successfully!"
