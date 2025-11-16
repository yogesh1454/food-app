-- V8__create_deliveries_table.sql
-- Create deliveries table for Delivery FSM (BE-003-22)

-- Drop existing table if it exists (dev environment)
DROP TABLE IF EXISTS deliveries CASCADE;

-- Drop existing trigger function if it exists
DROP FUNCTION IF EXISTS update_updated_at_column() CASCADE;

CREATE TABLE deliveries (
    delivery_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL UNIQUE REFERENCES orders(order_id),
    rider_id UUID,
    state VARCHAR(50) NOT NULL,
    delivery_fee DECIMAL(10, 2),
    search_radius_km DOUBLE PRECISION DEFAULT 2.0,
    retry_count INTEGER DEFAULT 0,
    
    -- Location data (JSONB)
    pickup_location JSONB,
    delivery_location JSONB,
    rider_location JSONB,
    
    -- Timestamps
    rider_assigned_at TIMESTAMP,
    rider_accepted_at TIMESTAMP,
    reached_restaurant_at TIMESTAMP,
    picked_up_at TIMESTAMP,
    delivered_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason TEXT,
    
    -- Metrics
    restaurant_wait_time_minutes INTEGER,
    total_delivery_time_minutes INTEGER,
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT chk_delivery_state CHECK (state IN (
        'PENDING', 'SEARCHING_RIDER', 'RIDER_ASSIGNED', 
        'RIDER_ACCEPTED', 'AT_RESTAURANT', 'PICKED_UP', 
        'OUT_FOR_DELIVERY', 'DELIVERED', 'FAILED'
    ))
);

-- Indexes
CREATE INDEX idx_deliveries_order_id ON deliveries(order_id);
CREATE INDEX idx_deliveries_rider_id ON deliveries(rider_id);
CREATE INDEX idx_deliveries_state ON deliveries(state);
CREATE INDEX idx_deliveries_state_rider ON deliveries(state, rider_id);
CREATE INDEX idx_deliveries_created_at ON deliveries(created_at);

-- Trigger for updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_deliveries_updated_at
    BEFORE UPDATE ON deliveries
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Comments
COMMENT ON TABLE deliveries IS 'Delivery FSM table tracking delivery lifecycle from rider assignment to completion';
COMMENT ON COLUMN deliveries.state IS 'Current delivery state: PENDING, SEARCHING_RIDER, RIDER_ASSIGNED, RIDER_ACCEPTED, AT_RESTAURANT, PICKED_UP, OUT_FOR_DELIVERY, DELIVERED, FAILED';
COMMENT ON COLUMN deliveries.search_radius_km IS 'Search radius for finding riders (expands on retry)';
COMMENT ON COLUMN deliveries.retry_count IS 'Number of times rider assignment was retried';
COMMENT ON COLUMN deliveries.restaurant_wait_time_minutes IS 'Time rider waited at restaurant';
COMMENT ON COLUMN deliveries.total_delivery_time_minutes IS 'Total time from creation to delivery';
