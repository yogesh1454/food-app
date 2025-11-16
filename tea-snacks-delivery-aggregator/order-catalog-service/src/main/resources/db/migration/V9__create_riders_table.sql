-- V9__create_riders_table.sql
-- Create riders table with PostGIS support (BE-003-24)

-- Enable PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE riders (
    rider_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(255),
    
    -- Location (PostGIS POINT)
    current_location geometry(Point, 4326),
    last_location_update TIMESTAMP,
    
    -- Status
    is_online BOOLEAN DEFAULT false,
    is_on_break BOOLEAN DEFAULT false,
    current_deliveries INTEGER DEFAULT 0,
    
    -- Metrics
    rating DECIMAL(3, 2) DEFAULT 5.00,
    total_deliveries INTEGER DEFAULT 0,
    completed_deliveries_today INTEGER DEFAULT 0,
    acceptance_rate DECIMAL(5, 2) DEFAULT 100.00,
    total_assignments INTEGER DEFAULT 0,
    accepted_assignments INTEGER DEFAULT 0,
    
    -- Penalty
    penalty_until TIMESTAMP,
    
    -- Device info
    device_token TEXT,
    device_platform VARCHAR(20),
    
    -- Audit
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT chk_rating CHECK (rating >= 0 AND rating <= 5),
    CONSTRAINT chk_acceptance_rate CHECK (acceptance_rate >= 0 AND acceptance_rate <= 100),
    CONSTRAINT chk_current_deliveries CHECK (current_deliveries >= 0)
);

-- Spatial index for location queries (critical for performance)
CREATE INDEX idx_riders_location ON riders USING GIST(current_location);

-- Other indexes
CREATE INDEX idx_riders_online ON riders(is_online, is_on_break) 
    WHERE is_online = true AND is_on_break = false;
CREATE INDEX idx_riders_rating ON riders(rating DESC);
CREATE INDEX idx_riders_acceptance_rate ON riders(acceptance_rate DESC);
CREATE INDEX idx_riders_phone ON riders(phone);

-- Trigger for updated_at
CREATE TRIGGER update_riders_updated_at
    BEFORE UPDATE ON riders
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Comments
COMMENT ON TABLE riders IS 'Riders table with PostGIS geospatial support for location-based queries';
COMMENT ON COLUMN riders.current_location IS 'PostGIS POINT geometry (SRID 4326 - WGS84)';
COMMENT ON COLUMN riders.acceptance_rate IS 'Percentage of assignments accepted (0-100)';
COMMENT ON COLUMN riders.penalty_until IS 'Rider cannot receive assignments until this timestamp';
