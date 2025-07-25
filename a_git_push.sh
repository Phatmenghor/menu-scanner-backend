#!/bin/bash

# Get current date and time
CURRENT_TIME=$(date "+%Y-%m-%d %H:%M:%S")

# Add all changes
git add .

# Commit with date-time message
git commit -m "Auto commit on $CURRENT_TIME"

# Push to development branch
git push origin main

echo "✅ Code pushed to 'main' branch at $CURRENT_TIME"
