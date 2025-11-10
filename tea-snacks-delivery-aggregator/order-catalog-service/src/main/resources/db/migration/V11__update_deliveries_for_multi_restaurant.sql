-- V11: Update deliveries table for multi-restaurant support
-- Add support for batched deliveries with multiple pickup locations

-- Add sub_order_ids array to track which sub-orders are in this delivery
ALTER TABLE deliveries ADD COLUMN IF NOT EXISTS sub_order_ids UUID[] DEFAULT '{}';

-- Add pickup_locations as JSONB to support multiple restaurant pickups
ALTER TABLE deliveries ADD COLUMN IF NOT EXISTS pickup_locations JSONB DEFAULT '[]'::jsonb;

-- Add optimized_route for batched deliveries
ALTER TABLE deliveries ADD COLUMN IF NOT EXISTS optimized_route JSONB DEFAULT '{}'::jsonb;

-- Add batch_size for analytics
ALTER TABLE deliveries ADD COLUMN IF NOT EXISTS batch_size INTEGER DEFAULT 1;

-- Add delivery strategy type
ALTER TABLE deliveries ADD COLUMN IF NOT EXISTS delivery_strategy VARCHAR(32) DEFAULT 'SINGLE_PICKUP';

-- Add constraint for batch_size
ALTER TABLE deliveries ADD CONSTRAINT deliveries_batch_size_positive CHECK (batch_size > 0);

-- Create index for sub_order_ids queries
CREATE INDEX idx_deliveries_sub_order_ids ON deliveries USING GIN(sub_order_ids);

-- Create index for delivery strategy
CREATE INDEX idx_deliveries_strategy ON deliveries(delivery_strategy);

-- Add comment
COMMENT ON COLUMN deliveries.sub_order_ids IS 'Array of sub-order IDs included in this delivery batch';
COMMENT ON COLUMN deliveries.pickup_locations IS 'Array of restaurant locations for pickup in optimized order';
COMMENT ON COLUMN deliveries.optimized_route IS 'Optimized route with distances and ETAs for batched deliveries';
COMMENT ON COLUMN deliveries.delivery_strategy IS 'Strategy used: SINGLE_PICKUP, SEQUENTIAL_PICKUP, or PARALLEL_DELIVERY';
