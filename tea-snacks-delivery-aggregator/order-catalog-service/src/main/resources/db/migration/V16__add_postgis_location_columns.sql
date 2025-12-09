-- V16__add_postgis_location_columns.sql
-- Add PostGIS geometry columns for pickup and delivery locations
-- Consistent with riders table approach (BE-003-24)

-- =====================================================
-- ORDERS TABLE - Add PostGIS pickup/delivery locations
-- =====================================================

-- Add pickup location as PostGIS POINT
ALTER TABLE orders 
ADD COLUMN IF NOT EXISTS pickup_location geometry(Point, 4326);

-- Add delivery location as PostGIS POINT  
ALTER TABLE orders 
ADD COLUMN IF NOT EXISTS delivery_geom geometry(Point, 4326);

-- Create spatial index for pickup location
CREATE INDEX IF NOT EXISTS idx_orders_pickup_location 
ON orders USING GIST(pickup_location);

-- Create spatial index for delivery location
CREATE INDEX IF NOT EXISTS idx_orders_delivery_geom 
ON orders USING GIST(delivery_geom);

-- Migrate existing decimal lat/lng to geometry (orders table has these columns)
UPDATE orders
SET pickup_location = ST_SetSRID(ST_MakePoint(pickup_longitude::float, pickup_latitude::float), 4326)
WHERE pickup_latitude IS NOT NULL 
  AND pickup_longitude IS NOT NULL 
  AND pickup_location IS NULL;

UPDATE orders
SET delivery_geom = ST_SetSRID(ST_MakePoint(delivery_longitude::float, delivery_latitude::float), 4326)
WHERE delivery_latitude IS NOT NULL 
  AND delivery_longitude IS NOT NULL 
  AND delivery_geom IS NULL;

-- =====================================================
-- DELIVERIES TABLE - Add PostGIS geometry columns
-- Note: Deliveries table uses JSONB for locations (pickup_location, delivery_location, rider_location)
-- We add separate geometry columns for spatial queries
-- =====================================================

-- Add pickup geometry as PostGIS POINT
ALTER TABLE deliveries 
ADD COLUMN IF NOT EXISTS pickup_geom geometry(Point, 4326);

-- Add delivery geometry as PostGIS POINT  
ALTER TABLE deliveries 
ADD COLUMN IF NOT EXISTS delivery_geom geometry(Point, 4326);

-- Add rider geometry for tracking
ALTER TABLE deliveries
ADD COLUMN IF NOT EXISTS rider_geom geometry(Point, 4326);

-- Create spatial indexes for deliveries
CREATE INDEX IF NOT EXISTS idx_deliveries_pickup_geom 
ON deliveries USING GIST(pickup_geom);

CREATE INDEX IF NOT EXISTS idx_deliveries_delivery_geom 
ON deliveries USING GIST(delivery_geom);

CREATE INDEX IF NOT EXISTS idx_deliveries_rider_geom 
ON deliveries USING GIST(rider_geom);

-- Note: No data migration for deliveries since existing location data is in JSONB format
-- The application code will populate geometry columns from JSONB when creating/updating deliveries

-- =====================================================
-- COMMENTS
-- =====================================================

COMMENT ON COLUMN orders.pickup_location IS 'PostGIS POINT geometry (SRID 4326) - Vendor branch pickup location';
COMMENT ON COLUMN orders.delivery_geom IS 'PostGIS POINT geometry (SRID 4326) - Customer delivery location';

COMMENT ON COLUMN deliveries.pickup_geom IS 'PostGIS POINT geometry (SRID 4326) - Restaurant pickup point';
COMMENT ON COLUMN deliveries.delivery_geom IS 'PostGIS POINT geometry (SRID 4326) - Customer delivery point';
COMMENT ON COLUMN deliveries.rider_geom IS 'PostGIS POINT geometry (SRID 4326) - Current rider location for tracking';

-- =====================================================
-- HELPER FUNCTIONS FOR DISTANCE CALCULATIONS
-- =====================================================

-- Function to calculate distance between two points in kilometers
CREATE OR REPLACE FUNCTION calculate_distance_km(
    point1 geometry,
    point2 geometry
) RETURNS DECIMAL AS $$
BEGIN
    RETURN ST_Distance(point1::geography, point2::geography) / 1000;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Function to check if a point is within radius of another point
CREATE OR REPLACE FUNCTION point_within_radius_km(
    center_point geometry,
    check_point geometry,
    radius_km DECIMAL
) RETURNS BOOLEAN AS $$
BEGIN
    RETURN ST_DWithin(center_point::geography, check_point::geography, radius_km * 1000);
END;
$$ LANGUAGE plpgsql IMMUTABLE;

COMMENT ON FUNCTION calculate_distance_km IS 'Calculate distance in kilometers between two PostGIS geometry points';
COMMENT ON FUNCTION point_within_radius_km IS 'Check if a point is within specified radius (km) of center point';
