-- V10: Create sub_orders table for multi-restaurant support
-- This enables parent-child order model where a single customer order
-- can contain multiple sub-orders from different restaurants

CREATE TABLE IF NOT EXISTS sub_orders (
    -- Primary Key
    sub_order_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Parent Order Reference
    parent_order_id UUID NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    
    -- Restaurant Reference
    restaurant_id UUID NOT NULL,
    
    -- State Management (Independent FSM per sub-order)
    state VARCHAR(32) NOT NULL,
    
    -- Items (stored as JSONB for flexibility)
    items JSONB NOT NULL,
    item_total DECIMAL(10,2) NOT NULL,
    
    -- Special Instructions
    special_instructions TEXT,
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
    accepted_at TIMESTAMP WITH TIME ZONE,
    ready_at TIMESTAMP WITH TIME ZONE,
    rejected_at TIMESTAMP WITH TIME ZONE,
    
    -- Preparation Time Estimates
    estimated_prep_time_minutes INTEGER,
    actual_prep_time_minutes INTEGER,
    
    -- Metadata (for extensibility)
    metadata JSONB DEFAULT '{}'::jsonb,
    
    -- Constraints
    CONSTRAINT sub_orders_item_total_positive CHECK (item_total >= 0),
    CONSTRAINT sub_orders_prep_time_positive CHECK (estimated_prep_time_minutes >= 0)
);

-- Indexes for performance
CREATE INDEX idx_sub_orders_parent_order_id ON sub_orders(parent_order_id);
CREATE INDEX idx_sub_orders_restaurant_id ON sub_orders(restaurant_id);
CREATE INDEX idx_sub_orders_state ON sub_orders(state);
CREATE INDEX idx_sub_orders_created_at ON sub_orders(created_at DESC);

-- Composite index for common queries
CREATE INDEX idx_sub_orders_parent_state ON sub_orders(parent_order_id, state);

-- GIN index for JSONB metadata queries
CREATE INDEX idx_sub_orders_metadata ON sub_orders USING GIN(metadata);

-- Add comment
COMMENT ON TABLE sub_orders IS 'Sub-orders for multi-restaurant orders. Each sub-order represents items from a single restaurant within a parent order.';
