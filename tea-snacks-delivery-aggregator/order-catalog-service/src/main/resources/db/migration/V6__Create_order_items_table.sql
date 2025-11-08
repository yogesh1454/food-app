-- V6__Create_order_items_table.sql
-- Order items table for order line items

CREATE TABLE order_items (
    order_item_id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    menu_item_id BIGINT NOT NULL REFERENCES menu_items(menu_item_id),
    
    -- Item snapshot
    item_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    price_at_order DECIMAL(10,2) NOT NULL,
    
    -- Customizations
    notes TEXT,
    customizations JSONB DEFAULT '[]'::jsonb,
    
    -- Audit
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_menu_item_id ON order_items(menu_item_id);
