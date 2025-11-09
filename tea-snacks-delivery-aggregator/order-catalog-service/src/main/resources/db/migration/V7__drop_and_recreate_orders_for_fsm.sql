-- V7__drop_and_recreate_orders_for_fsm.sql
-- Drop existing orders tables and recreate with FSM support

-- =====================================================
-- DROP EXISTING TABLES
-- =====================================================

DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS orders CASCADE;

-- =====================================================
-- ORDERS TABLE (Parent Order with FSM Support)
-- =====================================================
CREATE TABLE orders (
    order_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Order Type
    order_type VARCHAR(20) NOT NULL DEFAULT 'SINGLE',
    parent_order_id UUID REFERENCES orders(order_id),
    
    -- Customer Info
    customer_id UUID NOT NULL,
    
    -- FSM State
    state VARCHAR(32) NOT NULL,
    
    -- Pricing
    item_total DECIMAL(10,2) NOT NULL,
    delivery_charges DECIMAL(10,2) NOT NULL,
    platform_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    gst DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    discount DECIMAL(10,2) DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL,
    
    -- Payment
    payment_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(32),
    payment_transaction_id VARCHAR(100),
    
    -- Delivery Address
    delivery_address JSONB NOT NULL,
    delivery_latitude DECIMAL(10,8),
    delivery_longitude DECIMAL(11,8),
    
    -- Special Instructions
    special_instructions TEXT,
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    validated_at TIMESTAMP WITH TIME ZONE,
    payment_confirmed_at TIMESTAMP WITH TIME ZONE,
    accepted_at TIMESTAMP WITH TIME ZONE,
    preparing_started_at TIMESTAMP WITH TIME ZONE,
    ready_at TIMESTAMP WITH TIME ZONE,
    picked_up_at TIMESTAMP WITH TIME ZONE,
    delivered_at TIMESTAMP WITH TIME ZONE,
    cancelled_at TIMESTAMP WITH TIME ZONE,
    
    -- Estimated times
    estimated_prep_time_minutes INTEGER,
    estimated_delivery_time TIMESTAMP WITH TIME ZONE,
    
    -- Cancellation
    cancellation_reason VARCHAR(500),
    cancelled_by VARCHAR(20),
    
    -- Metadata
    metadata JSONB DEFAULT '{}'::jsonb,
    
    -- Constraints
    CONSTRAINT chk_order_state CHECK (state IN (
        'CREATED', 'VALIDATED', 'PAYMENT_CONFIRMED', 'PENDING_ACCEPTANCE',
        'ACCEPTED', 'PREPARING', 'READY_FOR_PICKUP', 'ASSIGNED_TO_RIDER',
        'PICKED_UP', 'DELIVERED', 'CLOSED', 'CANCELLED', 'REJECTED'
    )),
    CONSTRAINT chk_payment_status CHECK (payment_status IN (
        'PENDING', 'AUTHORIZED', 'CAPTURED', 'FAILED', 'REFUNDED', 'PARTIALLY_REFUNDED'
    )),
    CONSTRAINT chk_order_type CHECK (order_type IN ('SINGLE', 'MULTI_RESTAURANT'))
);

-- =====================================================
-- ORDER ITEMS TABLE (Enhanced Structure)
-- =====================================================
CREATE TABLE order_items (
    order_item_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    menu_item_id UUID NOT NULL,
    
    -- Item snapshot
    item_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    price_at_order DECIMAL(10,2) NOT NULL,
    
    -- Customizations
    notes TEXT,
    customizations JSONB DEFAULT '[]'::jsonb,
    
    -- Audit
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- =====================================================
-- SUB-ORDERS TABLE (Multi-Restaurant Support)
-- =====================================================
CREATE TABLE sub_orders (
    sub_order_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_order_id UUID NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    
    -- Vendor Info
    vendor_id UUID NOT NULL,
    branch_id BIGINT NOT NULL,
    
    -- Sub-order State
    state VARCHAR(32) NOT NULL,
    
    -- Pricing
    item_total DECIMAL(10,2) NOT NULL,
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    accepted_at TIMESTAMP WITH TIME ZONE,
    preparing_started_at TIMESTAMP WITH TIME ZONE,
    ready_at TIMESTAMP WITH TIME ZONE,
    
    -- Estimated prep time
    estimated_prep_time_minutes INTEGER,
    
    -- Metadata
    metadata JSONB DEFAULT '{}'::jsonb,
    
    CONSTRAINT chk_sub_order_state CHECK (state IN (
        'PENDING_ACCEPTANCE', 'ACCEPTED', 'PREPARING', 'READY_FOR_PICKUP', 'CANCELLED', 'REJECTED'
    ))
);

-- =====================================================
-- DELIVERIES TABLE (Delivery FSM)
-- =====================================================
CREATE TABLE deliveries (
    delivery_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    
    -- Rider Info
    rider_id UUID,
    
    -- Delivery State
    state VARCHAR(32) NOT NULL,
    
    -- Pickup Location
    pickup_latitude DECIMAL(10,8) NOT NULL,
    pickup_longitude DECIMAL(11,8) NOT NULL,
    pickup_address JSONB NOT NULL,
    
    -- Delivery Location
    delivery_latitude DECIMAL(10,8) NOT NULL,
    delivery_longitude DECIMAL(11,8) NOT NULL,
    delivery_address JSONB NOT NULL,
    
    -- Distance and Time
    distance_km DECIMAL(6,2),
    estimated_time_minutes INTEGER,
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    assigned_at TIMESTAMP WITH TIME ZONE,
    rider_accepted_at TIMESTAMP WITH TIME ZONE,
    picked_up_at TIMESTAMP WITH TIME ZONE,
    in_transit_at TIMESTAMP WITH TIME ZONE,
    arrived_at TIMESTAMP WITH TIME ZONE,
    delivered_at TIMESTAMP WITH TIME ZONE,
    cancelled_at TIMESTAMP WITH TIME ZONE,
    
    -- Cancellation
    cancellation_reason VARCHAR(500),
    
    -- Metadata
    metadata JSONB DEFAULT '{}'::jsonb,
    
    CONSTRAINT chk_delivery_state CHECK (state IN (
        'PENDING_ASSIGNMENT', 'ASSIGNED', 'RIDER_ACCEPTED', 'RIDER_ARRIVED_AT_RESTAURANT',
        'PICKED_UP', 'IN_TRANSIT', 'ARRIVED_AT_CUSTOMER', 'DELIVERED', 'CANCELLED'
    ))
);

-- =====================================================
-- ORDER STATE AUDIT TABLE
-- =====================================================
CREATE TABLE order_state_audit (
    audit_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    
    -- State Transition
    from_state VARCHAR(32),
    to_state VARCHAR(32) NOT NULL,
    trigger_name VARCHAR(50) NOT NULL,
    
    -- Context
    triggered_by UUID,
    triggered_by_role VARCHAR(20),
    
    -- Timestamp
    transitioned_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    
    -- Additional Info
    metadata JSONB DEFAULT '{}'::jsonb
);

-- =====================================================
-- DELIVERY STATE AUDIT TABLE
-- =====================================================
CREATE TABLE delivery_state_audit (
    audit_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    delivery_id UUID NOT NULL REFERENCES deliveries(delivery_id) ON DELETE CASCADE,
    
    -- State Transition
    from_state VARCHAR(32),
    to_state VARCHAR(32) NOT NULL,
    trigger_name VARCHAR(50) NOT NULL,
    
    -- Context
    triggered_by UUID,
    triggered_by_role VARCHAR(20),
    
    -- Timestamp
    transitioned_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    
    -- Additional Info
    metadata JSONB DEFAULT '{}'::jsonb
);

-- =====================================================
-- INDEXES
-- =====================================================

-- Orders indexes
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_state ON orders(state);
CREATE INDEX idx_orders_order_type ON orders(order_type);
CREATE INDEX idx_orders_parent_order_id ON orders(parent_order_id) WHERE parent_order_id IS NOT NULL;
CREATE INDEX idx_orders_created_at ON orders(created_at DESC);
CREATE INDEX idx_orders_payment_status ON orders(payment_status);

-- Order items indexes
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_menu_item_id ON order_items(menu_item_id);

-- Sub-orders indexes
CREATE INDEX idx_sub_orders_parent_order_id ON sub_orders(parent_order_id);
CREATE INDEX idx_sub_orders_vendor_id ON sub_orders(vendor_id);
CREATE INDEX idx_sub_orders_branch_id ON sub_orders(branch_id);
CREATE INDEX idx_sub_orders_state ON sub_orders(state);

-- Deliveries indexes
CREATE INDEX idx_deliveries_order_id ON deliveries(order_id);
CREATE INDEX idx_deliveries_rider_id ON deliveries(rider_id) WHERE rider_id IS NOT NULL;
CREATE INDEX idx_deliveries_state ON deliveries(state);
CREATE INDEX idx_deliveries_created_at ON deliveries(created_at DESC);

-- Audit indexes
CREATE INDEX idx_order_state_audit_order_id ON order_state_audit(order_id);
CREATE INDEX idx_order_state_audit_transitioned_at ON order_state_audit(transitioned_at DESC);
CREATE INDEX idx_delivery_state_audit_delivery_id ON delivery_state_audit(delivery_id);
CREATE INDEX idx_delivery_state_audit_transitioned_at ON delivery_state_audit(transitioned_at DESC);

-- =====================================================
-- COMMENTS
-- =====================================================

COMMENT ON TABLE orders IS 'Parent orders table with FSM support for order lifecycle management';
COMMENT ON TABLE order_items IS 'Order line items with menu item snapshots';
COMMENT ON TABLE sub_orders IS 'Sub-orders for multi-restaurant order support';
COMMENT ON TABLE deliveries IS 'Delivery information with FSM support for delivery lifecycle';
COMMENT ON TABLE order_state_audit IS 'Audit trail for order state transitions';
COMMENT ON TABLE delivery_state_audit IS 'Audit trail for delivery state transitions';

COMMENT ON COLUMN orders.state IS 'Current FSM state of the order';
COMMENT ON COLUMN orders.order_type IS 'SINGLE for single restaurant, MULTI_RESTAURANT for multiple restaurants';
COMMENT ON COLUMN orders.parent_order_id IS 'NULL for parent orders, references parent for sub-orders';
