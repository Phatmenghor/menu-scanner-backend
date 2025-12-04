#!/bin/bash

echo "=============================================="
echo "🚀 Building project using Java 17 (Docker)"
echo "=============================================="
echo ""

# Use your Windows absolute path
PROJECT_DIR="D:/e_menu/menu-scanner-backend"

# Docker image
DOCKER_IMAGE="maven:3.9.6-eclipse-temurin-17"

echo "📦 Pull Docker image if needed..."
docker pull $DOCKER_IMAGE
echo "✅ Docker image ready."
echo ""

echo "🔧 Step 1: Clean project..."
docker run --rm -v "$PROJECT_DIR":/app -w /app $DOCKER_IMAGE mvn clean || exit 1
echo "✅ Clean completed."
echo ""

echo "🔨 Step 2: Compile project..."
docker run --rm -v "$PROJECT_DIR":/app -w /app $DOCKER_IMAGE mvn compile || exit 1
echo "✅ Compile completed."
echo ""

echo "📦 Step 3: Install project..."
docker run --rm -v "$PROJECT_DIR":/app -w /app $DOCKER_IMAGE mvn install || exit 1
echo "✅ Install completed."
echo ""

echo "=============================================="
echo "🎉 BUILD SUCCESSFUL using Java 17 (Docker)"
echo "=============================================="
