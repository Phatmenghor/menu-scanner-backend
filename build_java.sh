#!/bin/bash

echo "======================================"
echo "🚀 Starting Maven Build Process"
echo "======================================"

echo "🧹 Step 1: Cleaning project..."
mvn clean
if [ $? -ne 0 ]; then
  echo "❌ Clean failed"
  exit 1
fi

echo "⚙️ Step 2: Compiling source code..."
mvn compile
if [ $? -ne 0 ]; then
  echo "❌ Compile failed"
  exit 1
fi

echo "🧪 Step 3: Running tests..."
mvn test
if [ $? -ne 0 ]; then
  echo "❌ Tests failed"
  exit 1
fi

echo "📦 Step 4: Packaging JAR..."
mvn package
if [ $? -ne 0 ]; then
  echo "❌ Package failed"
  exit 1
fi

echo "📥 Step 5: Installing to local Maven repository..."
mvn install
if [ $? -ne 0 ]; then
  echo "❌ Install failed"
  exit 1
fi

echo "======================================"
echo "✅ Maven build SUCCESSFUL!"
echo "📍 JAR location: target/"
echo "======================================"
