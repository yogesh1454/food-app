-- V15: Add additional columns to order_items for complete order summary display
-- These columns capture menu item details at the time of order for historical accuracy

-- Add item_description column
ALTER TABLE order_items 
ADD COLUMN IF NOT EXISTS item_description VARCHAR(500);

-- Add image_url column for item thumbnail
ALTER TABLE order_items 
ADD COLUMN IF NOT EXISTS image_url VARCHAR(512);

-- Add category_name column
ALTER TABLE order_items 
ADD COLUMN IF NOT EXISTS category_name VARCHAR(100);

-- Add subtotal column (quantity * price_at_order)
ALTER TABLE order_items 
ADD COLUMN IF NOT EXISTS subtotal NUMERIC(10, 2);

-- Update existing rows to calculate subtotal
UPDATE order_items 
SET subtotal = quantity * price_at_order 
WHERE subtotal IS NULL;

-- Add NOT NULL constraint to subtotal after populating existing data
-- Note: Not adding NOT NULL to avoid migration issues with existing data
-- ALTER TABLE order_items ALTER COLUMN subtotal SET NOT NULL;

-- Add index on category_name for filtering
CREATE INDEX IF NOT EXISTS idx_order_items_category ON order_items(category_name);

COMMENT ON COLUMN order_items.item_description IS 'Description of the item captured at order time';
COMMENT ON COLUMN order_items.image_url IS 'Item thumbnail URL captured at order time';
COMMENT ON COLUMN order_items.category_name IS 'Category name captured at order time (e.g., Beverages, Snacks)';
COMMENT ON COLUMN order_items.subtotal IS 'Calculated subtotal = quantity * price_at_order';
