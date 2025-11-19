#!/bin/bash

###############################################################################
# Script: init-database.sh
# Description: Initialize PostgreSQL database with schema and seed data
# Usage: Run this script from EC2 instance after stack creation
# 
# This is a PLACEHOLDER script. Customize it with your actual database schema.
###############################################################################

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== Database Initialization Script ===${NC}"
echo ""

# Load environment variables
if [ -f "/opt/nashtto/.env" ]; then
    echo -e "${GREEN}✓ Loading environment variables from /opt/nashtto/.env${NC}"
    source /opt/nashtto/.env
else
    echo -e "${RED}✗ Error: Environment file not found at /opt/nashtto/.env${NC}"
    echo "Please ensure the CloudFormation stack has been created successfully."
    exit 1
fi

# Verify required environment variables
if [ -z "$DB_HOST" ] || [ -z "$DB_USERNAME" ] || [ -z "$DB_NAME" ]; then
    echo -e "${RED}✗ Error: Required environment variables not set${NC}"
    echo "Required: DB_HOST, DB_USERNAME, DB_NAME, DB_PASSWORD"
    exit 1
fi

echo -e "${GREEN}✓ Environment variables loaded${NC}"
echo "  Database Host: $DB_HOST"
echo "  Database Name: $DB_NAME"
echo "  Database User: $DB_USERNAME"
echo ""

# Test database connection
echo -e "${YELLOW}Testing database connection...${NC}"
export PGPASSWORD=$DB_PASSWORD

if psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME -c "SELECT version();" > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Database connection successful${NC}"
else
    echo -e "${RED}✗ Error: Cannot connect to database${NC}"
    echo "Please check:"
    echo "  1. RDS instance is in 'available' state"
    echo "  2. Security group allows connections from this EC2"
    echo "  3. Database credentials are correct"
    exit 1
fi

echo ""

###############################################################################
# CUSTOMIZE THIS SECTION WITH YOUR DATABASE SCHEMA
###############################################################################

echo -e "${BLUE}=== Creating Database Schema ===${NC}"
echo ""

# Example: Create tables
echo -e "${YELLOW}Creating tables...${NC}"

psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME << 'EOF'

-- ============================================================================
-- USERS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    phone VARCHAR(20),
    role VARCHAR(20) DEFAULT 'customer',
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);

-- ============================================================================
-- RESTAURANTS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS restaurants (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    address TEXT NOT NULL,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    phone VARCHAR(20),
    email VARCHAR(100),
    status VARCHAR(20) DEFAULT 'active',
    rating DECIMAL(3, 2) DEFAULT 0.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_restaurants_status ON restaurants(status);
CREATE INDEX idx_restaurants_location ON restaurants(latitude, longitude);

-- ============================================================================
-- MENU_ITEMS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS menu_items (
    id SERIAL PRIMARY KEY,
    restaurant_id INTEGER NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50),
    price DECIMAL(10, 2) NOT NULL,
    image_url VARCHAR(500),
    is_available BOOLEAN DEFAULT true,
    preparation_time INTEGER DEFAULT 15,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_menu_items_restaurant ON menu_items(restaurant_id);
CREATE INDEX idx_menu_items_category ON menu_items(category);
CREATE INDEX idx_menu_items_available ON menu_items(is_available);

-- ============================================================================
-- ORDERS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS orders (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id),
    restaurant_id INTEGER NOT NULL REFERENCES restaurants(id),
    order_number VARCHAR(50) UNIQUE NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',
    total_amount DECIMAL(10, 2) NOT NULL,
    delivery_address TEXT NOT NULL,
    delivery_instructions TEXT,
    payment_method VARCHAR(50),
    payment_status VARCHAR(20) DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_restaurant ON orders(restaurant_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created ON orders(created_at);

-- ============================================================================
-- ORDER_ITEMS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS order_items (
    id SERIAL PRIMARY KEY,
    order_id INTEGER NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    menu_item_id INTEGER NOT NULL REFERENCES menu_items(id),
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    special_instructions TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_menu_item ON order_items(menu_item_id);

-- ============================================================================
-- REVIEWS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS reviews (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id),
    restaurant_id INTEGER NOT NULL REFERENCES restaurants(id),
    order_id INTEGER REFERENCES orders(id),
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_reviews_restaurant ON reviews(restaurant_id);
CREATE INDEX idx_reviews_user ON reviews(user_id);

-- ============================================================================
-- GRANT PERMISSIONS
-- ============================================================================
-- Note: Replace 'app_user' with your application database user if different

-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO app_user;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO app_user;

EOF

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Tables created successfully${NC}"
else
    echo -e "${RED}✗ Error creating tables${NC}"
    exit 1
fi

echo ""

###############################################################################
# INSERT SEED DATA (Optional)
###############################################################################

echo -e "${BLUE}=== Inserting Seed Data ===${NC}"
echo ""

echo -e "${YELLOW}Inserting sample data...${NC}"

psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME << 'EOF'

-- Sample admin user (password: admin123 - CHANGE IN PRODUCTION!)
INSERT INTO users (username, email, password_hash, full_name, role)
VALUES 
    ('admin', 'admin@nashtto.com', '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYCxcXDFGRq', 'Admin User', 'admin'),
    ('testuser', 'test@nashtto.com', '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYCxcXDFGRq', 'Test User', 'customer')
ON CONFLICT (username) DO NOTHING;

-- Sample restaurant
INSERT INTO restaurants (name, description, address, phone, email)
VALUES 
    ('Sample Tea House', 'Authentic tea and snacks', '123 Main St, City, State 12345', '555-0100', 'info@sampletea.com'),
    ('Snack Corner', 'Quick bites and beverages', '456 Oak Ave, City, State 12345', '555-0200', 'hello@snackcorner.com')
ON CONFLICT DO NOTHING;

-- Sample menu items
INSERT INTO menu_items (restaurant_id, name, description, category, price, is_available)
VALUES 
    (1, 'Masala Chai', 'Traditional Indian spiced tea', 'Beverages', 2.99, true),
    (1, 'Samosa', 'Crispy pastry with spiced filling', 'Snacks', 3.99, true),
    (1, 'Green Tea', 'Premium green tea', 'Beverages', 2.49, true),
    (2, 'Sandwich', 'Fresh veggie sandwich', 'Snacks', 5.99, true),
    (2, 'Coffee', 'Freshly brewed coffee', 'Beverages', 3.49, true)
ON CONFLICT DO NOTHING;

EOF

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Seed data inserted successfully${NC}"
else
    echo -e "${YELLOW}⚠ Warning: Some seed data may not have been inserted (this is ok if data already exists)${NC}"
fi

echo ""

###############################################################################
# VERIFICATION
###############################################################################

echo -e "${BLUE}=== Verifying Database Setup ===${NC}"
echo ""

# Count tables
TABLE_COUNT=$(psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';")
echo "Tables created: $TABLE_COUNT"

# Count users
USER_COUNT=$(psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME -t -c "SELECT COUNT(*) FROM users;")
echo "Users: $USER_COUNT"

# Count restaurants
RESTAURANT_COUNT=$(psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME -t -c "SELECT COUNT(*) FROM restaurants;")
echo "Restaurants: $RESTAURANT_COUNT"

# Count menu items
MENU_COUNT=$(psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME -t -c "SELECT COUNT(*) FROM menu_items;")
echo "Menu items: $MENU_COUNT"

echo ""

###############################################################################
# DATABASE MIGRATION TOOLS (Optional)
###############################################################################

echo -e "${BLUE}=== Database Migration Tools ===${NC}"
echo ""
echo "For production use, consider using a database migration tool:"
echo ""
echo "1. Flyway (Java)"
echo "   - Installation: https://flywaydb.org/documentation/usage/commandline/"
echo "   - Place SQL migrations in: db/migration/V1__description.sql"
echo "   - Run: flyway -url=jdbc:postgresql://$DB_HOST:5432/$DB_NAME -user=$DB_USERNAME migrate"
echo ""
echo "2. Liquibase (Java)"
echo "   - Installation: https://www.liquibase.org/download"
echo "   - Create changelog: db/changelog/changelog-master.xml"
echo "   - Run: liquibase update"
echo ""
echo "3. Alembic (Python)"
echo "   - Installation: pip install alembic"
echo "   - Initialize: alembic init migrations"
echo "   - Run: alembic upgrade head"
echo ""
echo "4. Sequelize (Node.js)"
echo "   - Installation: npm install sequelize-cli"
echo "   - Run: npx sequelize-cli db:migrate"
echo ""

###############################################################################
# COMPLETION
###############################################################################

echo -e "${GREEN}==================================${NC}"
echo -e "${GREEN}✓ Database initialization complete!${NC}"
echo -e "${GREEN}==================================${NC}"
echo ""
echo "Database Details:"
echo "  Host: $DB_HOST"
echo "  Database: $DB_NAME"
echo "  User: $DB_USERNAME"
echo ""
echo "Next Steps:"
echo "  1. Review the schema in PostgreSQL: psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME"
echo "  2. Update this script with your actual database schema"
echo "  3. Integrate with your application's database migrations"
echo "  4. Change default passwords for security"
echo ""
echo "Sample Credentials (CHANGE IN PRODUCTION):"
echo "  Username: admin / Password: admin123"
echo "  Username: testuser / Password: admin123"
echo ""

# Cleanup
unset PGPASSWORD


