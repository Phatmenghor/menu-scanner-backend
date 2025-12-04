#!/bin/bash

echo "=============================================="
echo "🚀 Building project using Java 17 (Docker)"
echo "=============================================="
echo ""

# Project directory (current folder)
PROJECT_DIR="$PWD"

# Docker image
DOCKER_IMAGE="maven:3.9.6-eclipse-temurin-17"

echo "📦 Pulling Docker image $DOCKER_IMAGE (if not exists)..."
docker pull $DOCKER_IMAGE
echo "✅ Docker image ready."
echo ""

echo "🔧 Step 1: Cleaning project..."
docker run --rm -v "$PROJECT_DIR":/app -w /app $DOCKER_IMAGE mvn clean
echo "✅ Clean completed."
echo ""

echo "🔨 Step 2: Compiling project..."
docker run --rm -v "$PROJECT_DIR":/app -w /app $DOCKER_IMAGE mvn compile
echo "✅ Compile completed."
echo ""

echo "📦 Step 3: Installing project into local Maven repository..."
docker run --rm -v "$PROJECT_DIR":/app -w /app $DOCKER_IMAGE mvn install
echo "✅ Install completed."
echo ""

echo "=============================================="
echo "🎉 BUILD SUCCESSFUL using Java 17 (Docker)"
echo "=============================================="
