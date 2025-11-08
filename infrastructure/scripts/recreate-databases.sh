#!/bin/bash

# Script to drop and recreate all PostgreSQL databases for Tea & Snacks Delivery Aggregator
# This will reset all databases to a clean state with updated migration scripts

set -e

CONTAINER_NAME="tea-snacks-postgres"
DB_USER="tea_snacks_user"
MAIN_DB="tea_snacks_db"

echo "🗑️  Dropping and recreating all databases..."
echo "=============================================="

# Array of databases to recreate
databases=(
    "order_catalog_db"
    "user_management_db"
    "payment_management_db"
    "delivery_management_db"
    "notification_db"
    "search_discovery_db"
)

# Drop and recreate each database
for db in "${databases[@]}"; do
    echo ""
    echo "📦 Processing: $db"
    
    # Drop database
    docker exec $CONTAINER_NAME psql -U $DB_USER -d $MAIN_DB -c "DROP DATABASE IF EXISTS $db;" 2>&1 | grep -v "does not exist" || true
    
    # Create database
    docker exec $CONTAINER_NAME psql -U $DB_USER -d $MAIN_DB -c "CREATE DATABASE $db OWNER $DB_USER;"
    
    echo "✅ $db recreated"
done

echo ""
echo "=============================================="
echo "✅ All databases recreated successfully!"
echo ""
echo "📋 Databases recreated:"
for db in "${databases[@]}"; do
    echo "   - $db"
done
echo ""
echo "🔄 Next steps:"
echo "   1. Restart your microservices"
echo "   2. Flyway will automatically run migrations"
echo "   3. Run your E2E tests"
echo ""
