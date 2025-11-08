-- V5__Create_orders_table.sql
-- Orders table for customer orders

CREATE TABLE orders (
    order_id BIGSERIAL PRIMARY KEY,
    customer_id UUID NOT NULL,
    branch_id BIGINT NOT NULL REFERENCES vendor_branches(branch_id),
    delivery_partner_id UUID,
    
    -- Order status
    order_status VARCHAR(50) DEFAULT 'PENDING',
    payment_status VARCHAR(50) DEFAULT 'PENDING',
    
    -- Pricing
    total_amount DECIMAL(10,2) NOT NULL,
    
    -- Delivery details (JSONB)
    delivery_details JSONB DEFAULT '{
        "type": "STANDARD",
        "address": {},
        "instructions": "",
        "train_details": null,
        "bus_details": null,
        "factory_details": null
    }'::jsonb,
    
    -- Timestamps
    ordered_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    estimated_delivery_time TIMESTAMP WITH TIME ZONE,
    delivered_at TIMESTAMP WITH TIME ZONE,
    
    -- Special instructions
    special_instructions TEXT,
    
    -- Metadata
    metadata JSONB DEFAULT '{}'::jsonb,
    
    -- Audit
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_branch_id ON orders(branch_id);
CREATE INDEX idx_orders_status ON orders(order_status);
CREATE INDEX idx_orders_ordered_at ON orders(ordered_at DESC);
