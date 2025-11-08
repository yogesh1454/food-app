#!/bin/bash

# Script to fix Flyway migration issues
# This script handles cases where tables exist but Flyway doesn't know about them

echo "🔧 Fixing Flyway migration issues..."

# Check if PostgreSQL container is running
if ! docker ps | grep -q "tea-snacks-postgres"; then
    echo "❌ PostgreSQL container is not running. Please start it first."
    exit 1
fi

# Function to check if table exists
table_exists() {
    local table_name=$1
    docker exec -it tea-snacks-postgres psql -U tea_snacks_user -d tea_snacks_db -t -c "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = '$table_name');" | grep -q "t"
}

# Function to check if migration is recorded
migration_exists() {
    local version=$1
    docker exec -it tea-snacks-postgres psql -U tea_snacks_user -d tea_snacks_db -t -c "SELECT EXISTS (SELECT FROM flyway_schema_history WHERE version = '$version');" | grep -q "t"
}

# Function to add migration to history
add_migration_to_history() {
    local version=$1
    local description=$2
    local script=$3
    local checksum=$4
    
    echo "📝 Adding migration $version to Flyway history..."
    docker exec -it tea-snacks-postgres psql -U tea_snacks_user -d tea_snacks_db -c "INSERT INTO flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) VALUES ((SELECT COALESCE(MAX(installed_rank), 0) + 1 FROM flyway_schema_history), '$version', '$description', 'SQL', '$script', $checksum, 'tea_snacks_user', NOW(), 0, true);"
}

# Check and fix guest_users table
if table_exists "guest_users"; then
    echo "✅ guest_users table exists"
    
    if ! migration_exists "3"; then
        echo "⚠️  Migration V3 not recorded in Flyway history"
        add_migration_to_history "3" "Create Guest Users Table" "V3__Create_Guest_Users_Table.sql" "-2103016496"
        echo "✅ Migration V3 added to Flyway history"
    else
        echo "✅ Migration V3 already recorded"
    fi
else
    echo "❌ guest_users table does not exist"
fi

# Check and fix otp_sessions table
if table_exists "otp_sessions"; then
    echo "✅ otp_sessions table exists"
    
    if ! migration_exists "2"; then
        echo "⚠️  Migration V2 not recorded in Flyway history"
        add_migration_to_history "2" "Create Otp Sessions Table" "V2__Create_Otp_Sessions_Table.sql" "0"
        echo "✅ Migration V2 added to Flyway history"
    else
        echo "✅ Migration V2 already recorded"
    fi
else
    echo "❌ otp_sessions table does not exist"
fi

echo "🎉 Flyway migration fix completed!"
echo ""
echo "📋 Current migration status:"
docker exec -it tea-snacks-postgres psql -U tea_snacks_user -d tea_snacks_db -c "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;"

echo ""
echo "💡 You can now start the application without migration issues." 