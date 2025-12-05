-- =====================================================
-- Migration V12: Create Search & Discovery Tables
-- =====================================================
-- Features:
-- - PostGIS for geospatial queries
-- - pg_trgm for fuzzy search
-- - Full-text search vectors
-- - Blended ranking support
-- - Multiple image sizes (JSONB)
-- - Trending/popular metrics
-- =====================================================

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS postgis;

-- Drop existing tables if they exist (for clean recreation)
DROP TABLE IF EXISTS search_popular_queries CASCADE;
DROP TABLE IF EXISTS search_analytics CASCADE;
DROP TABLE IF EXISTS search_menu_items CASCADE;
DROP TABLE IF EXISTS search_vendors CASCADE;

-- =====================================================
-- Table 1: search_vendors
-- Purpose: Denormalized vendor/branch index for fast search
-- =====================================================

CREATE TABLE search_vendors (
    -- Primary Key
    branch_id BIGINT PRIMARY KEY,
    vendor_id BIGINT NOT NULL,
    
    -- Basic Info (denormalized from vendors + vendor_branches)
    vendor_name VARCHAR(255) NOT NULL,
    branch_name VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    
    -- Location (PostGIS for geospatial queries)
    location GEOMETRY(Point, 4326) NOT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    city VARCHAR(100) NOT NULL,
    area VARCHAR(100),
    address JSONB,
    
    -- Full-Text Search Index (maintained by trigger)
    search_vector tsvector,
    
    -- Searchable Attributes (with size constraints)
    cuisine TEXT[] CHECK (array_length(cuisine, 1) <= 20),
    tags TEXT[] CHECK (array_length(tags, 1) <= 20),
    
    -- Metrics & Filters
    rating DECIMAL(3, 2),
    total_ratings INTEGER DEFAULT 0,
    is_open BOOLEAN NOT NULL DEFAULT false,
    is_active BOOLEAN NOT NULL DEFAULT true,
    
    -- Delivery Info
    delivery_time_min INTEGER,
    delivery_time_max INTEGER,
    delivery_fee DECIMAL(10, 2),
    min_order_value DECIMAL(10, 2),
    
    -- Popularity Metrics (for blended ranking)
    order_count INTEGER DEFAULT 0,
    popularity_score DECIMAL(5, 2) DEFAULT 0,
    normalized_popularity DECIMAL(5, 4) GENERATED ALWAYS AS (
        LEAST(1.0, (COALESCE(rating, 0) / 5.0) * LOG(GREATEST(order_count, 0) + 1) / 10.0)
    ) STORED,
    
    -- Images (stored in S3, multiple sizes as JSONB)
    images JSONB DEFAULT '[]',
    primary_image VARCHAR(500),
    
    -- Sync Metadata
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_synced_at TIMESTAMP NOT NULL DEFAULT NOW(),
    sync_version INTEGER DEFAULT 1,
    
    -- Foreign Keys
    CONSTRAINT fk_search_vendors_branch 
        FOREIGN KEY (branch_id) 
        REFERENCES vendor_branches(branch_id) 
        ON DELETE CASCADE
);

-- Indexes for search_vendors

-- 1. Full-Text Search (for exact and stemmed word matching)
CREATE INDEX idx_search_vendors_fts ON search_vendors USING GIN(search_vector);

-- 2. Trigram Indexes (for fuzzy/typo-tolerant matching)
CREATE INDEX idx_search_vendors_trgm_vendor_name ON search_vendors 
    USING GIN(vendor_name gin_trgm_ops);
CREATE INDEX idx_search_vendors_trgm_branch_name ON search_vendors 
    USING GIN(branch_name gin_trgm_ops);
CREATE INDEX idx_search_vendors_trgm_display_name ON search_vendors 
    USING GIN(display_name gin_trgm_ops);

-- 3. Geospatial Index (CRITICAL for filter-first strategy)
CREATE INDEX idx_search_vendors_location ON search_vendors USING GIST(location);
CREATE INDEX idx_search_vendors_city_location ON search_vendors(city, location) 
    WHERE is_active = true;

-- 4. Filter Indexes
CREATE INDEX idx_search_vendors_city ON search_vendors(city);
CREATE INDEX idx_search_vendors_rating ON search_vendors(rating DESC) 
    WHERE is_active = true;
CREATE INDEX idx_search_vendors_popularity ON search_vendors(normalized_popularity DESC, rating DESC);
CREATE INDEX idx_search_vendors_status ON search_vendors(is_open, is_active);

-- 5. Composite Index for Blended Ranking
CREATE INDEX idx_search_vendors_ranking ON search_vendors(
    normalized_popularity DESC, 
    rating DESC, 
    total_ratings DESC
) WHERE is_active = true;

-- =====================================================
-- Table 2: search_menu_items
-- Purpose: Denormalized menu item index with vendor context
-- =====================================================

CREATE TABLE search_menu_items (
    -- Primary Key
    menu_item_id BIGINT PRIMARY KEY,
    
    -- Menu Item Info
    item_name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    category VARCHAR(100) NOT NULL,
    
    -- Full-Text Search Index (maintained by trigger)
    search_vector tsvector,
    
    -- Vendor Context (denormalized for efficient queries)
    branch_id BIGINT NOT NULL,
    branch_name VARCHAR(255) NOT NULL,
    vendor_id BIGINT NOT NULL,
    vendor_name VARCHAR(255) NOT NULL,
    
    -- Location (for proximity filtering)
    branch_location GEOMETRY(Point, 4326) NOT NULL,
    branch_latitude DECIMAL(10, 8),
    branch_longitude DECIMAL(11, 8),
    city VARCHAR(100) NOT NULL,
    
    -- Availability & Attributes (with size constraints)
    is_available BOOLEAN NOT NULL DEFAULT true,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    tags TEXT[] CHECK (array_length(tags, 1) <= 20),
    dietary_info TEXT[] CHECK (array_length(dietary_info, 1) <= 10),
    preparation_time_minutes INTEGER,
    
    -- Popularity Metrics (for blended ranking)
    order_count INTEGER DEFAULT 0,
    order_count_7d INTEGER DEFAULT 0,    -- For trending/top ordered items
    order_count_30d INTEGER DEFAULT 0,   -- For popular items
    rating DECIMAL(3, 2),
    popularity_score DECIMAL(5, 2) DEFAULT 0,
    normalized_popularity DECIMAL(5, 4) GENERATED ALWAYS AS (
        LEAST(1.0, (COALESCE(rating, 3.0) / 5.0) * LOG(GREATEST(order_count, 0) + 1) / 8.0)
    ) STORED,
    trending_score DECIMAL(5, 4) GENERATED ALWAYS AS (
        CASE 
            WHEN order_count > 0 THEN 
                LEAST(1.0, (order_count_7d::DECIMAL / NULLIF(order_count, 0)) * 10)
            ELSE 0
        END
    ) STORED,
    
    -- Images (stored in S3, multiple sizes as JSONB)
    images JSONB DEFAULT '[]',
    primary_image VARCHAR(500),
    
    -- Sync Metadata
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_synced_at TIMESTAMP NOT NULL DEFAULT NOW(),
    sync_version INTEGER DEFAULT 1,
    
    -- Foreign Keys
    CONSTRAINT fk_search_menu_items_item 
        FOREIGN KEY (menu_item_id) 
        REFERENCES menu_items(menu_item_id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_search_menu_items_branch 
        FOREIGN KEY (branch_id) 
        REFERENCES vendor_branches(branch_id) 
        ON DELETE CASCADE
);

-- Indexes for search_menu_items

-- 1. Full-Text Search
CREATE INDEX idx_search_items_fts ON search_menu_items USING GIN(search_vector);

-- 2. Geospatial
CREATE INDEX idx_search_items_location ON search_menu_items USING GIST(branch_location);

-- 3. Trigram indexes for fuzzy search
CREATE INDEX idx_search_items_trgm_name ON search_menu_items 
    USING GIN(item_name gin_trgm_ops);

-- 4. Filter Indexes
CREATE INDEX idx_search_items_branch ON search_menu_items(branch_id);
CREATE INDEX idx_search_items_category ON search_menu_items(category);
CREATE INDEX idx_search_items_price ON search_menu_items(price);
CREATE INDEX idx_search_items_available ON search_menu_items(is_available, is_deleted) 
    WHERE is_available = true AND is_deleted = false;
CREATE INDEX idx_search_items_popularity ON search_menu_items(normalized_popularity DESC, rating DESC);

-- 5. Composite index for blended ranking
CREATE INDEX idx_search_items_ranking ON search_menu_items(
    normalized_popularity DESC,
    rating DESC,
    order_count DESC
) WHERE is_available = true AND is_deleted = false;

-- 6. Indexes for trending/popular items
CREATE INDEX idx_search_items_trending ON search_menu_items(
    trending_score DESC,
    order_count_7d DESC
) WHERE is_available = true AND is_deleted = false;

CREATE INDEX idx_search_items_popular ON search_menu_items(
    order_count_30d DESC,
    rating DESC
) WHERE is_available = true AND is_deleted = false;

-- =====================================================
-- Table 3: search_analytics
-- Purpose: Track search queries for analytics and optimization
-- =====================================================

CREATE TABLE search_analytics (
    id BIGSERIAL PRIMARY KEY,
    
    -- Query Info
    query_text VARCHAR(500) NOT NULL,
    query_type VARCHAR(50) NOT NULL,
    search_context VARCHAR(50),
    
    -- User Context
    user_id UUID,
    session_id VARCHAR(100),
    
    -- Location Context
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    city VARCHAR(100),
    
    -- Filters Applied (stored as JSON)
    filters JSONB,
    
    -- Results
    result_count INTEGER NOT NULL,
    zero_results BOOLEAN GENERATED ALWAYS AS (result_count = 0) STORED,
    
    -- Performance
    response_time_ms INTEGER NOT NULL,
    cache_hit BOOLEAN DEFAULT false,
    
    -- Ranking Metrics
    avg_fts_score DECIMAL(5, 4),
    avg_fuzzy_score DECIMAL(5, 4),
    ranking_strategy VARCHAR(50),
    
    -- Timestamp
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes for search_analytics
CREATE INDEX idx_analytics_query ON search_analytics(query_text);
CREATE INDEX idx_analytics_user ON search_analytics(user_id);
CREATE INDEX idx_analytics_zero ON search_analytics(zero_results) WHERE zero_results = true;
CREATE INDEX idx_analytics_created ON search_analytics(created_at DESC);
CREATE INDEX idx_analytics_city_created ON search_analytics(city, created_at DESC);

-- =====================================================
-- Table 4: search_popular_queries
-- Purpose: Cache popular searches for auto-complete
-- =====================================================

CREATE TABLE search_popular_queries (
    id SERIAL PRIMARY KEY,
    
    query_text VARCHAR(500) NOT NULL,
    query_type VARCHAR(50) NOT NULL,
    
    -- Popularity Metrics
    search_count INTEGER NOT NULL DEFAULT 0,
    click_through_rate DECIMAL(5, 4),
    
    -- Time Period
    period VARCHAR(20) NOT NULL,
    period_start TIMESTAMP NOT NULL,
    period_end TIMESTAMP NOT NULL,
    
    -- Context
    city VARCHAR(100),
    
    -- Display
    display_text VARCHAR(500),
    suggestion_order INTEGER,
    
    -- Timestamp
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes for search_popular_queries
CREATE UNIQUE INDEX idx_popular_query_unique ON search_popular_queries(query_text, period, city);
CREATE INDEX idx_popular_period ON search_popular_queries(period, city);
CREATE INDEX idx_popular_count ON search_popular_queries(search_count DESC);

-- =====================================================
-- Triggers for maintaining search vectors
-- =====================================================

-- Function to update search vector for vendors
CREATE OR REPLACE FUNCTION update_vendor_search_vector()
RETURNS TRIGGER AS $$
BEGIN
    NEW.search_vector = (
        setweight(to_tsvector('english', COALESCE(NEW.vendor_name, '')), 'A') ||
        setweight(to_tsvector('english', COALESCE(NEW.branch_name, '')), 'A') ||
        setweight(to_tsvector('english', COALESCE(NEW.display_name, '')), 'B') ||
        setweight(to_tsvector('english', COALESCE(array_to_string(NEW.cuisine, ' '), '')), 'C') ||
        setweight(to_tsvector('english', COALESCE(array_to_string(NEW.tags, ' '), '')), 'D')
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Function to update search vector for menu items
CREATE OR REPLACE FUNCTION update_menu_item_search_vector()
RETURNS TRIGGER AS $$
BEGIN
    NEW.search_vector = (
        setweight(to_tsvector('english', COALESCE(NEW.item_name, '')), 'A') ||
        setweight(to_tsvector('english', COALESCE(NEW.description, '')), 'B') ||
        setweight(to_tsvector('english', COALESCE(NEW.category, '')), 'C') ||
        setweight(to_tsvector('english', COALESCE(array_to_string(NEW.tags, ' '), '')), 'D')
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create triggers
CREATE TRIGGER trigger_update_vendor_search_vector
    BEFORE INSERT OR UPDATE ON search_vendors
    FOR EACH ROW EXECUTE FUNCTION update_vendor_search_vector();

CREATE TRIGGER trigger_update_menu_item_search_vector
    BEFORE INSERT OR UPDATE ON search_menu_items
    FOR EACH ROW EXECUTE FUNCTION update_menu_item_search_vector();

-- =====================================================
-- Comments for documentation
-- =====================================================

COMMENT ON TABLE search_vendors IS 'Denormalized vendor/branch index for fast location-based and text search';
COMMENT ON TABLE search_menu_items IS 'Denormalized menu item index with vendor context for fast search';
COMMENT ON TABLE search_analytics IS 'Track search queries for analytics and optimization';
COMMENT ON TABLE search_popular_queries IS 'Cache popular searches for auto-complete';

COMMENT ON COLUMN search_vendors.search_vector IS 'Full-text search vector (maintained by trigger)';
COMMENT ON COLUMN search_vendors.normalized_popularity IS 'Normalized popularity score for blended ranking (auto-generated)';
COMMENT ON COLUMN search_vendors.images IS 'JSONB array of image URLs with multiple sizes';
COMMENT ON COLUMN search_menu_items.trending_score IS 'Trending score based on recent orders (auto-generated)';
COMMENT ON COLUMN search_menu_items.normalized_popularity IS 'Normalized popularity score for blended ranking (auto-generated)';


