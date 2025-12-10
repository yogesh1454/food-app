-- V17__remove_redundant_lat_lng_columns.sql
-- Remove redundant decimal lat/lng columns now that we use PostGIS geometry
-- Data has been migrated to geometry columns in V16

-- =====================================================
-- ORDERS TABLE - Remove old decimal lat/lng columns
-- =====================================================

-- Remove pickup lat/lng (now using pickup_location geometry)
ALTER TABLE orders DROP COLUMN IF EXISTS pickup_latitude;
ALTER TABLE orders DROP COLUMN IF EXISTS pickup_longitude;

-- Remove delivery lat/lng (now using delivery_geom geometry)
ALTER TABLE orders DROP COLUMN IF EXISTS delivery_latitude;
ALTER TABLE orders DROP COLUMN IF EXISTS delivery_longitude;

-- =====================================================
-- COMMENTS
-- =====================================================

COMMENT ON TABLE orders IS 'Orders table - now using PostGIS geometry for location data';
