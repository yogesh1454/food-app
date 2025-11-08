-- V4__Create_menu_items_table.sql
-- Menu items table for branch-specific menu

CREATE TABLE menu_items (
    menu_item_id BIGSERIAL PRIMARY KEY,
    branch_id BIGINT NOT NULL REFERENCES vendor_branches(branch_id) ON DELETE CASCADE,
    
    -- Item details
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    category VARCHAR(100) NOT NULL,
    
    -- Images (JSONB)
    images JSONB DEFAULT '{
        "primary": null,
        "gallery": []
    }'::jsonb,
    
    -- Availability
    is_available BOOLEAN DEFAULT TRUE,
    preparation_time_minutes INTEGER DEFAULT 15,
    
    -- Flexible metadata
    metadata JSONB DEFAULT '{
        "nutritional_info": {},
        "customizations": [],
        "allergens": [],
        "dietary_tags": []
    }'::jsonb,
    
    -- Tags
    tags TEXT[] DEFAULT ARRAY[]::TEXT[],
    
    -- Soft delete
    is_deleted BOOLEAN DEFAULT FALSE,
    
    -- Audit
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_menu_items_branch_id ON menu_items(branch_id);
CREATE INDEX idx_menu_items_category ON menu_items(category);
CREATE INDEX idx_menu_items_available ON menu_items(is_available) 
    WHERE is_deleted = FALSE;
CREATE INDEX idx_menu_items_tags ON menu_items USING GIN(tags);
