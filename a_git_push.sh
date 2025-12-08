#!/bin/bash

# Get current date and time
CURRENT_TIME=$(date "+%Y-%m-%d %H:%M:%S")

# Get current branch name
BRANCH=$(git rev-parse --abbrev-ref HEAD)

# Get changed files list
CHANGES=$(git status --short | awk '{print $2}' | tr '\n' ' ')

git fetch

# Add all changes
git add .

# Commit with auto message
git commit -m "[$BRANCH] Auto commit on $CURRENT_TIME | Files: $CHANGES"

# Push to remote branch
git push origin $BRANCH

echo "✅ Auto push completed to '$BRANCH' at $CURRENT_TIME"
