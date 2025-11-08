#!/bin/bash

# Stop minimal infrastructure for Epic 2 development

echo "🛑 Stopping minimal infrastructure..."

# Change to docker directory
cd "$(dirname "$0")"

# Stop and remove containers
docker-compose -f docker-compose-minimal.yml down

echo "✅ Minimal infrastructure stopped"
echo ""
echo "💡 To start again: ./start-minimal.sh"
echo "💡 To remove volumes (clean slate): docker-compose -f docker-compose-minimal.yml down -v"
