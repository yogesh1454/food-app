# 9\. Database Schema

For PostgreSQL, we will use standard SQL DDL. Elasticsearch and VectorDB will manage their own indexing/vector stores, populated via Kafka consumers from PostgreSQL data changes.

```sql
-- Schema for User Management Service (UMS)
CREATE TABLE users (
    user_id UUID PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    user_type VARCHAR(50) NOT NULL, -- 'CUSTOMER', 'VENDOR', 'DELIVERY_PARTNER', 'ADMIN'
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    address JSONB, -- Stores street, city, state, zip, lat, long
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) DEFAULT 'ACTIVE', -- 'ACTIVE', 'INACTIVE', 'SUSPENDED'
    company_id UUID, -- For B2B integration
    internal_delivery_point VARCHAR(255) -- For B2B integration
);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_phone ON users (phone_number);
CREATE INDEX idx_users_user_type ON users (user_type);
CREATE INDEX idx_users_company_id ON users (company_id);

-- Schema for Order & Catalog/Vendor Service (OCVMS)
CREATE TABLE vendors (
    vendor_id UUID PRIMARY KEY,
    user_id UUID UNIQUE NOT NULL REFERENCES users(user_id), -- Link to user account for login
    name VARCHAR(255) NOT NULL,
    description TEXT,
    address JSONB NOT NULL, -- Stores street, city, state, zip, lat, long
    phone_number VARCHAR(20),
    email VARCHAR(255),
    rating DECIMAL(2,1) DEFAULT 0.0,
    status VARCHAR(50) DEFAULT 'PENDING_APPROVAL', -- 'ACTIVE', 'INACTIVE', 'PENDING_APPROVAL'
    operating_hours JSONB, -- Array of objects: [{day: "MON", open: "09:00", close: "18:00"}]
    is_open BOOLEAN DEFAULT FALSE,
    menu_version INTEGER DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_vendors_user_id ON vendors (user_id);
CREATE INDEX idx_vendors_name ON vendors (name);
CREATE INDEX idx_vendors_status ON vendors (status);
CREATE INDEX idx_vendors_location ON vendors USING GIST (CAST(address->>'latitude' AS DOUBLE PRECISION), CAST(address->>'longitude' AS DOUBLE PRECISION)); -- Requires PostGIS for spatial indexing

CREATE TABLE menu_items (
    menu_item_id UUID PRIMARY KEY,
    vendor_id UUID NOT NULL REFERENCES vendors(vendor_id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    category VARCHAR(100) NOT NULL,
    image_url VARCHAR(500),
    is_available BOOLEAN DEFAULT TRUE,
    preparation_time_minutes INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_menu_items_vendor_id ON menu_items (vendor_id);
CREATE INDEX idx_menu_items_name ON menu_items (name);
CREATE INDEX idx_menu_items_category ON menu_items (category);

CREATE TABLE orders (
    order_id UUID PRIMARY KEY,
    customer_id UUID NOT NULL REFERENCES users(user_id),
    vendor_id UUID NOT NULL REFERENCES vendors(vendor_id),
    delivery_partner_id UUID REFERENCES users(user_id), -- Optional, assigned by DMS
    order_status VARCHAR(50) NOT NULL, -- 'PENDING', 'ACCEPTED', 'PREPARING', 'READY_FOR_PICKUP', 'ON_THE_WAY', 'DELIVERED', 'CANCELLED', 'REJECTED'
    total_amount DECIMAL(10,2) NOT NULL,
    payment_status VARCHAR(50) NOT NULL, -- 'PENDING', 'PAID', 'FAILED', 'REFUNDED'
    delivery_address JSONB NOT NULL, -- Snapshot of address at time of order
    ordered_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    estimated_delivery_time TIMESTAMP WITH TIME ZONE,
    delivered_at TIMESTAMP WITH TIME ZONE,
    special_instructions TEXT,
    train_number VARCHAR(50),
    coach_number VARCHAR(50),
    seat_number VARCHAR(50),
    station_code VARCHAR(50),
    scheduled_arrival_time TIMESTAMP WITH TIME ZONE,
    bus_operator VARCHAR(100),
    bus_number VARCHAR(50),
    scheduled_stop_time TIMESTAMP WITH TIME ZONE,
    company_id UUID, -- For B2B orders
    internal_delivery_point VARCHAR(255) -- For B2B orders
);

CREATE INDEX idx_orders_customer_id ON orders (customer_id);
CREATE INDEX idx_orders_vendor_id ON orders (vendor_id);
CREATE INDEX idx_orders_delivery_partner_id ON orders (delivery_partner_id);
CREATE INDEX idx_orders_status ON orders (order_status);
CREATE INDEX idx_orders_ordered_at ON orders (ordered_at);
CREATE INDEX idx_orders_station_code ON orders (station_code);
CREATE INDEX idx_orders_company_id ON orders (company_id);

CREATE TABLE order_items (
    order_item_id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(order_id),
    menu_item_id UUID NOT NULL REFERENCES menu_items(menu_item_id),
    quantity INTEGER NOT NULL,
    price_at_order DECIMAL(10,2) NOT NULL,
    notes TEXT,
    CONSTRAINT unique_order_item UNIQUE (order_id, menu_item_id) -- Ensures only one entry per item per order
);

CREATE INDEX idx_order_items_order_id ON order_items (order_id);
CREATE INDEX idx_order_items_menu_item_id ON order_items (menu_item_id);

-- Schema for Payment Service (PMS)
CREATE TABLE payment_transactions (
    transaction_id UUID PRIMARY KEY,
    order_id UUID UNIQUE NOT NULL REFERENCES orders(order_id), -- One-to-one with order
    payment_method VARCHAR(50) NOT NULL, -- 'UPI', 'CASH', 'CARD', 'WALLET'
    amount DECIMAL(10,2) NOT NULL,
    transaction_status VARCHAR(50) NOT NULL, -- 'PENDING', 'SUCCESS', 'FAILED', 'REFUNDED'
    gateway_transaction_id VARCHAR(255),
    transaction_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    refund_id UUID -- Link to refund record if applicable
);

CREATE INDEX idx_payment_transactions_order_id ON payment_transactions (order_id);
CREATE INDEX idx_payment_transactions_status ON payment_transactions (transaction_status);

-- Schema for Delivery Service (DMS) - Delivery Partner Details (assuming delivery partner user_type in 'users' table)
-- This table specifically holds delivery-partner related attributes, distinct from their user login info.
CREATE TABLE delivery_partners (
    delivery_partner_id UUID PRIMARY KEY,
    user_id UUID UNIQUE NOT NULL REFERENCES users(user_id), -- Link to user account for login
    vehicle_type VARCHAR(50) NOT NULL, -- 'BIKE', 'SCOOTER', 'BICYCLE', 'CAR'
    current_location POINT, -- Stores (longitude, latitude) - requires PostGIS `point` type
    availability_status VARCHAR(50) DEFAULT 'OFFLINE', -- 'ONLINE', 'OFFLINE', 'ON_DELIVERY'
    rating DECIMAL(2,1) DEFAULT 0.0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_delivery_partners_user_id ON delivery_partners (user_id);
CREATE INDEX idx_delivery_partners_status ON delivery_partners (availability_status);
CREATE INDEX idx_delivery_partners_location ON delivery_partners USING GIST (current_location); -- Requires PostGIS for spatial indexing

-- Note: SDS will use Elasticsearch and VectorDB primarily. Data will be denormalized and indexed from PostgreSQL via Kafka events.
-- Elasticsearch Index (conceptual, actual mapping defined in SDS service code):
-- Index: vendors_index
-- Fields: vendor_id, name, description, address.latitude, address.longitude, rating, is_open, category (from menu items)
--
-- Index: menu_items_index
-- Fields: menu_item_id, vendor_id, vendor_name, name, description, price, category, is_available, preparation_time_minutes
--
-- VectorDB (conceptual, actual schema depends on chosen VectorDB):
-- Collection: item_embeddings
-- Fields: menu_item_id, embedding (vector array), metadata (e.g., category, vendor_id)
--
-- Collection: user_embeddings (for personalized recommendations)
-- Fields: user_id, embedding (vector array), metadata
```
