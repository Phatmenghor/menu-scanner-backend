#!/bin/bash

# Get current date and time
CURRENT_TIME=$(date "+%Y-%m-%d %H:%M:%S")

git fetch

# Push to development branch
git pull origin main

echo "✅ Code pull form 'main' branch at $CURRENT_TIME"
