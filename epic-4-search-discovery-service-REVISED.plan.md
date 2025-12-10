# Epic 4: Search & Discovery - Domain within Order-Catalog-Service (REVISED)

## Overview

Implement comprehensive search functionality as a **new domain** within the existing `order-catalog-service`, following the established domain-driven design pattern. Uses separate search tables for read optimization, PostgreSQL full-text search, PostGIS for geospatial queries, **event-driven synchronization**, **blended ranking algorithm**, and 4 mobile-optimized REST APIs.

## 🆕 Key Revisions Incorporated

### 1. **Blended Ranking Algorithm** ⭐
- Weighted multi-factor scoring: FTS (50%) + Fuzzy (30%) + Popularity (15%) + Proximity (5%)
- Continuous scoring instead of binary FTS-or-Fuzzy approach
- Configurable weights per search context

### 2. **Event-Driven Synchronization** ⭐
- Asynchronous AWS SNS/SQS-based sync to avoid data inconsistency
- Decoupled writes: transactional updates succeed independently
- Retry mechanism for failed index updates
- Dead Letter Queue (DLQ) for problematic events

### 3. **Geospatial Optimization** ⭐
- Filter-first strategy: geospatial filters applied before FTS/Fuzzy
- Regional tile-based caching for common locations
- Bounded box pre-filtering to reduce search space

### 4. **Data Quality Controls** ⭐
- Tag/cuisine array size limits (max 20 items)
- Data validation before sync to search tables
- Clean denormalization process

### 5. **AWS Native Architecture** ⭐
- AWS SNS/SQS instead of Kafka (fully managed, cost-effective)
- S3 + CloudFront for image storage and delivery
- CloudWatch for monitoring and alerting

### 6. **Enhanced API Responses** ⭐
- Multiple image sizes (thumbnail, small, medium, large, original)
- Complete item details (rating, nutrition, prep time, dietary info)
- 4-section Discovery Feed (Nearby, Popular, Recommended, Trending)

---

## Architecture Decision

### **Why Search Domain within Order-Catalog-Service?**

- ✅ **Resource Efficient:** No separate microservice infrastructure
- ✅ **Same Database:** No cross-database queries, simpler transactions
- ✅ **Domain-Driven Design:** Follows existing package structure
- ✅ **Easier Development:** Single codebase, shared utilities
- ✅ **Lower Latency:** No network calls between services
- ✅ **Shared Cache:** Can reuse Redis connection and cache utilities

### **Why Separate Search Tables?**

- ✅ **Read Optimization:** Denormalized for fast queries
- ✅ **Independent Indexing:** Heavy GIN/GIST indexes don't impact transactional tables
- ✅ **No Lock Contention:** Search queries don't block order writes
- ✅ **Clear Separation:** Transactional vs analytical data

### **Why Event-Driven Sync (AWS SNS/SQS)?** 🆕

- ✅ **Data Consistency:** Transactional writes always succeed first
- ✅ **Decoupling:** Search index failures don't block core operations
- ✅ **Retry Logic:** Failed syncs automatically retry until successful (SQS visibility timeout)
- ✅ **Audit Trail:** All events are logged for debugging
- ✅ **Scalability:** Fully managed, auto-scales with load
- ✅ **AWS Native:** Seamless integration with CloudWatch, IAM, and other AWS services
- ✅ **Cost-Effective:** Pay per use, no idle broker costs

---

## Package Structure

```
order-catalog-service/
└── src/main/java/com/teadelivery/ordercatalog/
    ├── order/              (existing)
    ├── delivery/           (existing)
    ├── menu/               (existing)
    ├── vendor/             (existing)
    └── search/             ⭐ NEW DOMAIN
        ├── controller/
        │   └── SearchController.java
        ├── service/
        │   ├── DiscoveryFeedService.java
        │   ├── UnifiedSearchService.java          🆕 Blended Ranking
        │   ├── VendorMenuService.java
        │   ├── RecommendationService.java
        │   ├── SearchCacheService.java            🆕 Regional Caching
        │   ├── SearchAnalyticsService.java
        │   └── SearchRankingService.java          🆕 NEW
        ├── sync/
        │   ├── VendorSearchIndexService.java
        │   ├── MenuItemSearchIndexService.java
        │   └── SearchEventConsumer.java           🆕 NEW - SQS Consumer
        ├── event/                                 🆕 NEW PACKAGE
        │   ├── SearchEventPublisher.java          (SNS Publisher)
        │   ├── VendorIndexEvent.java
        │   └── MenuItemIndexEvent.java
        ├── repository/
        │   ├── SearchVendorRepository.java
        │   ├── SearchMenuItemRepository.java
        │   ├── SearchAnalyticsRepository.java
        │   └── SearchPopularQueriesRepository.java
        ├── model/
        │   ├── SearchVendor.java
        │   ├── SearchMenuItem.java
        │   ├── SearchQuery.java
        │   └── PopularQuery.java
        └── dto/
            ├── DiscoveryFeedResponse.java
            ├── SearchRequest.java
            ├── SearchResponse.java
            ├── VendorMenuResponse.java
            ├── RecommendationResponse.java
            └── SearchRankingConfig.java           🆕 NEW
```

---

## Fuzzy Search Strategy (pg_trgm Extension)

**Why pg_trgm for Typo Tolerance:**

- ✅ Handles typos: "chai" matches "chae", "chay", "chia"
- ✅ Similarity scoring: 0-1 score based on character trigrams
- ✅ Fast with GIN indexes
- ✅ Works alongside full-text search

**Trigram Matching:**

```
"chai" → trigrams: ["  c", " ch", "cha", "hai", "ai "]
"chae" → trigrams: ["  c", " ch", "cha", "hae", "ae "]
Similarity = (common trigrams / total trigrams) ≈ 0.6 (60% match)
```

**Search Strategy - Hybrid + Blended Ranking:** 🆕

1. **Execute Both:** Run FTS and Fuzzy searches in parallel (not fallback)
2. **Calculate Blended Score:** Weighted combination of multiple factors
3. **Rank Continuously:** No binary FTS-or-Fuzzy, all results scored uniformly

**Blended Ranking Formula:** 🆕

```
TotalScore = (W_fts × ts_rank) + 
             (W_fuzzy × similarity) + 
             (W_geo × proximity_factor) + 
             (W_pop × popularity_score)

Where:
- W_fts = 0.50      (Full-text search weight)
- W_fuzzy = 0.30    (Fuzzy match weight)
- W_geo = 0.05      (Geospatial proximity weight)
- W_pop = 0.15      (Popularity weight)

proximity_factor = 1 / (1 + distance_km)
popularity_score = normalized(rating × log(order_count + 1))
```

---

## Database Schema (Same Database as Order-Catalog)

### **Table 1: `search_vendors`**

**Purpose:** Denormalized vendor/branch index for fast location-based and text search with fuzzy matching

```sql
-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS postgis;

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
    
    -- Full-Text Search Index (auto-generated for exact/stemmed matching)
    search_vector tsvector GENERATED ALWAYS AS (
        setweight(to_tsvector('english', COALESCE(vendor_name, '')), 'A') ||
        setweight(to_tsvector('english', COALESCE(branch_name, '')), 'A') ||
        setweight(to_tsvector('english', COALESCE(display_name, '')), 'B') ||
        setweight(to_tsvector('english', array_to_string(cuisine, ' ')), 'C') ||
        setweight(to_tsvector('english', array_to_string(tags, ' ')), 'D')
    ) STORED,
    
    -- Searchable Attributes (with size constraints) 🆕
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
    
    -- Popularity Metrics (for blended ranking) 🆕
    order_count INTEGER DEFAULT 0,
    popularity_score DECIMAL(5, 2) DEFAULT 0,
    normalized_popularity DECIMAL(5, 4) GENERATED ALWAYS AS (
        LEAST(1.0, (rating / 5.0) * LOG(order_count + 1) / 10.0)
    ) STORED,
    
    -- Images (stored in S3, multiple sizes) 🆕
    images JSONB DEFAULT '[]',
    -- Structure: [
    --   {
    --     "type": "cover",
    --     "urls": {
    --       "original": "https://cdn.foodapp.com/vendors/101/cover_original.jpg",
    --       "large": "https://cdn.foodapp.com/vendors/101/cover_large.jpg",
    --       "medium": "https://cdn.foodapp.com/vendors/101/cover_medium.jpg",
    --       "small": "https://cdn.foodapp.com/vendors/101/cover_small.jpg",
    --       "thumbnail": "https://cdn.foodapp.com/vendors/101/cover_thumbnail.jpg"
    --     },
    --     "dimensions": {"width": 1920, "height": 1080},
    --     "displayOrder": 1
    --   },
    --   {"type": "logo", "urls": {...}, "displayOrder": 2}
    -- ]
    primary_image VARCHAR(500),  -- Quick access to main thumbnail
    
    -- Sync Metadata
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_synced_at TIMESTAMP NOT NULL DEFAULT NOW(),
    sync_version INTEGER DEFAULT 1,  🆕
    
    -- Foreign Keys
    CONSTRAINT fk_search_vendors_branch 
        FOREIGN KEY (branch_id) 
        REFERENCES vendor_branches(branch_id) 
        ON DELETE CASCADE
);

-- Indexes for Performance

-- 1. Full-Text Search (for exact and stemmed word matching)
CREATE INDEX idx_search_vendors_fts ON search_vendors USING GIN(search_vector);

-- 2. Trigram Indexes (for fuzzy/typo-tolerant matching)
CREATE INDEX idx_search_vendors_trgm_vendor_name ON search_vendors 
    USING GIN(vendor_name gin_trgm_ops);
CREATE INDEX idx_search_vendors_trgm_branch_name ON search_vendors 
    USING GIN(branch_name gin_trgm_ops);
CREATE INDEX idx_search_vendors_trgm_display_name ON search_vendors 
    USING GIN(display_name gin_trgm_ops);

-- 3. Geospatial Index (CRITICAL for filter-first strategy) 🆕
CREATE INDEX idx_search_vendors_location ON search_vendors USING GIST(location);
CREATE INDEX idx_search_vendors_city_location ON search_vendors(city, location) 
    WHERE is_active = true;

-- 4. Filter Indexes
CREATE INDEX idx_search_vendors_city ON search_vendors(city);
CREATE INDEX idx_search_vendors_rating ON search_vendors(rating DESC) 
    WHERE is_active = true;
CREATE INDEX idx_search_vendors_popularity ON search_vendors(normalized_popularity DESC, rating DESC);
CREATE INDEX idx_search_vendors_status ON search_vendors(is_open, is_active);

-- 5. Composite Index for Blended Ranking 🆕
CREATE INDEX idx_search_vendors_ranking ON search_vendors(
    normalized_popularity DESC, 
    rating DESC, 
    total_ratings DESC
) WHERE is_active = true;
```

---

### **Blended Ranking Query** 🆕

**Filter-First Geospatial + Blended Scoring:**

```sql
-- Configuration
SET pg_trgm.similarity_threshold = 0.3;

-- Step 1: Apply geospatial filter FIRST (filter-first strategy)
WITH nearby_vendors AS (
    SELECT 
        branch_id,
        vendor_name,
        branch_name,
        display_name,
        rating,
        location,
        normalized_popularity,
        order_count,
        ST_Distance(
            location::geography,
            ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography
        ) / 1000.0 AS distance_km
    FROM search_vendors
    WHERE 
        -- GEOSPATIAL FILTER FIRST
        ST_DWithin(
            location::geography,
            ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography,
            :radius_meters
        )
        AND is_active = true
        AND (:cityFilter IS NULL OR city = :cityFilter)
),
-- Step 2: Calculate FTS and Fuzzy scores
scored_vendors AS (
    SELECT 
        nv.*,
        -- FTS Score (0-1 normalized)
        COALESCE(
            ts_rank_cd(sv.search_vector, query) / 
            (SELECT MAX(ts_rank_cd(sv.search_vector, query)) + 0.01 
             FROM search_vendors sv, to_tsquery('english', :query || ':*') query
             WHERE sv.branch_id IN (SELECT branch_id FROM nearby_vendors)),
            0
        ) AS fts_score,
        
        -- Fuzzy Score (0-1)
        GREATEST(
            COALESCE(similarity(sv.vendor_name, :query), 0),
            COALESCE(similarity(sv.branch_name, :query), 0),
            COALESCE(similarity(sv.display_name, :query), 0)
        ) AS fuzzy_score,
        
        -- Proximity Factor (0-1)
        1.0 / (1.0 + nv.distance_km) AS proximity_factor
    FROM nearby_vendors nv
    JOIN search_vendors sv ON sv.branch_id = nv.branch_id
    CROSS JOIN to_tsquery('english', :query || ':*') query
)
-- Step 3: Blended Ranking
SELECT 
    branch_id,
    vendor_name,
    branch_name,
    rating,
    distance_km,
    -- Blended Score Calculation
    (
        (0.50 * fts_score) +
        (0.30 * fuzzy_score) +
        (0.05 * proximity_factor) +
        (0.15 * normalized_popularity)
    ) AS total_score,
    fts_score,
    fuzzy_score,
    proximity_factor,
    normalized_popularity
FROM scored_vendors
WHERE 
    (fts_score > 0 OR fuzzy_score > 0.3)  -- At least some relevance
ORDER BY 
    total_score DESC,
    rating DESC,
    distance_km ASC
LIMIT :limit;
```

**Query Execution Plan:**
1. **PostGIS Filter** → Narrows to ~100-500 vendors within radius
2. **FTS + Fuzzy Parallel** → Scores all nearby vendors
3. **Blended Ranking** → Single unified score
4. **Sort** → By total_score (single sort operation)

---

### **Event-Driven Data Synchronization Strategy (AWS SNS/SQS)** 🆕

**Architecture:**

```
┌─────────────────────────────────────────────────────────────┐
│              Order-Catalog-Service (Publisher)               │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  VendorService / MenuItemService                            │
│  1. Update transactional DB ✓                               │
│  2. Publish to SNS Topic ─────────────────────────────┐    │
│                                                         │     │
└─────────────────────────────────────────────────────────│────┘
                                                          │
                     AWS SNS Topics                       │
         (vendor-index-events, menu-index-events)        │
                                                          │
                     ┌────────────────────────────────────┘
                     │ (fanout to multiple queues)
                     │
         ┌───────────┴────────────┬──────────────────┐
         │                        │                  │
         ▼                        ▼                  ▼
    SQS Queue              SQS Queue          SQS Queue
  (Search Index)        (Analytics)      (Notifications)
         │
         │ Long Polling (20s)
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│    SearchEventConsumer (SQS Listener)                       │
├─────────────────────────────────────────────────────────────┤
│  1. Poll SQS messages (batch of 10)                         │
│  2. Validate Data (array size limits)                       │
│  3. Update search_vendors / search_menu_items               │
│  4. Invalidate Cache (tile-based)                           │
│  5. Delete message from queue (ack)                         │
│                                                              │
│  Error Handling:                                            │
│  - Visibility timeout: 30 seconds                           │
│  - Max receive count: 3 attempts                            │
│  - DLQ: Dead Letter Queue for failed messages               │
│  - CloudWatch alarms: Alert on DLQ accumulation            │
└─────────────────────────────────────────────────────────────┘
```

**Benefits of SNS/SQS over Kafka:**
- ✅ Fully managed (no broker maintenance)
- ✅ Auto-scales with load
- ✅ Pay per use (cost-effective)
- ✅ AWS integration (CloudWatch, IAM, X-Ray)
- ✅ Fanout pattern (SNS → multiple SQS queues)

**SNS Event Schema:**

```json
{
  "eventId": "uuid",
  "eventType": "VENDOR_CREATED | VENDOR_UPDATED | VENDOR_DELETED",
  "timestamp": "2024-11-26T10:00:00Z",
  "version": 1,
  "payload": {
    "branchId": 101,
    "vendorId": 1,
    "vendorName": "Chai Express",
    "branchName": "MG Road",
    "latitude": 12.9716,
    "longitude": 77.5946,
    "city": "Bangalore",
    "cuisine": ["Tea", "Snacks"],
    "tags": ["Fast Delivery"],
    "images": [
      {
        "type": "cover",
        "urls": {
          "original": "https://cdn.foodapp.com/vendors/101/cover_original.jpg",
          "thumbnail": "https://cdn.foodapp.com/vendors/101/cover_thumbnail.jpg"
        }
      }
    ],
    "rating": 4.5,
    "orderCount": 1250,
    "isOpen": true,
    "isActive": true
  }
}
```

**Implementation Flow:**

1. **VendorService** updates transactional DB and publishes event to SNS
2. **SNS** fans out to multiple SQS queues (Search, Analytics, Notifications)
3. **SearchEventConsumer** polls SQS, validates data (array size limits), updates search tables
4. **Cache Invalidation** clears tile-based cache for affected city
5. **Message Deletion** acknowledges successful processing (or retries on failure)

**Benefits of SNS/SQS Sync:**
- ✅ Transactional writes never fail due to search index issues
- ✅ Search index updates retry automatically (SQS visibility timeout)
- ✅ Fully managed, no infrastructure maintenance
- ✅ Audit trail logged in CloudWatch
- ✅ Auto-scales with load

---

### **Table 2: `search_menu_items`**

**Purpose:** Denormalized menu item index with vendor context for fast search

```sql
CREATE TABLE search_menu_items (
    -- Primary Key
    menu_item_id BIGINT PRIMARY KEY,
    
    -- Menu Item Info
    item_name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    category VARCHAR(100) NOT NULL,
    
    -- Full-Text Search Index (auto-generated)
    search_vector tsvector GENERATED ALWAYS AS (
        setweight(to_tsvector('english', COALESCE(item_name, '')), 'A') ||
        setweight(to_tsvector('english', COALESCE(description, '')), 'B') ||
        setweight(to_tsvector('english', COALESCE(category, '')), 'C') ||
        setweight(to_tsvector('english', array_to_string(tags, ' ')), 'D')
    ) STORED,
    
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
    
    -- Availability & Attributes (with size constraints) 🆕
    is_available BOOLEAN NOT NULL DEFAULT true,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    tags TEXT[] CHECK (array_length(tags, 1) <= 20),
    dietary_info TEXT[] CHECK (array_length(dietary_info, 1) <= 10),
    preparation_time_minutes INTEGER,
    
    -- Popularity Metrics (for blended ranking) 🆕
    order_count INTEGER DEFAULT 0,
    order_count_7d INTEGER DEFAULT 0,    -- For trending/top ordered items
    order_count_30d INTEGER DEFAULT 0,   -- For popular items
    rating DECIMAL(3, 2),
    popularity_score DECIMAL(5, 2) DEFAULT 0,
    normalized_popularity DECIMAL(5, 4) GENERATED ALWAYS AS (
        LEAST(1.0, (COALESCE(rating, 3.0) / 5.0) * LOG(order_count + 1) / 8.0)
    ) STORED,
    trending_score DECIMAL(5, 4) GENERATED ALWAYS AS (
        CASE 
            WHEN order_count > 0 THEN 
                LEAST(1.0, (order_count_7d::DECIMAL / NULLIF(order_count, 0)) * 10)
            ELSE 0
        END
    ) STORED,
    
    -- Images (stored in S3, multiple sizes) 🆕
    images JSONB DEFAULT '[]',
    -- Same structure as vendor images
    primary_image VARCHAR(500),  -- Quick access to main thumbnail
    
    -- Sync Metadata
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_synced_at TIMESTAMP NOT NULL DEFAULT NOW(),
    sync_version INTEGER DEFAULT 1,  🆕
    
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

-- Indexes
CREATE INDEX idx_search_items_fts ON search_menu_items USING GIN(search_vector);
CREATE INDEX idx_search_items_location ON search_menu_items USING GIST(branch_location);
CREATE INDEX idx_search_items_branch ON search_menu_items(branch_id);
CREATE INDEX idx_search_items_category ON search_menu_items(category);
CREATE INDEX idx_search_items_price ON search_menu_items(price);
CREATE INDEX idx_search_items_available ON search_menu_items(is_available, is_deleted) 
    WHERE is_available = true AND is_deleted = false;
CREATE INDEX idx_search_items_popularity ON search_menu_items(normalized_popularity DESC, rating DESC);

-- Trigram indexes for fuzzy search 🆕
CREATE INDEX idx_search_items_trgm_name ON search_menu_items 
    USING GIN(item_name gin_trgm_ops);

-- Composite index for blended ranking 🆕
CREATE INDEX idx_search_items_ranking ON search_menu_items(
    normalized_popularity DESC,
    rating DESC,
    order_count DESC
) WHERE is_available = true AND is_deleted = false;

-- Indexes for trending/popular items 🆕
CREATE INDEX idx_search_items_trending ON search_menu_items(
    trending_score DESC,
    order_count_7d DESC
) WHERE is_available = true AND is_deleted = false;

CREATE INDEX idx_search_items_popular ON search_menu_items(
    order_count_30d DESC,
    rating DESC
) WHERE is_available = true AND is_deleted = false;
```

---

### **Table 3: `search_analytics`**

**Purpose:** Track search queries for analytics and optimization

```sql
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
    
    -- Ranking Metrics 🆕
    avg_fts_score DECIMAL(5, 4),
    avg_fuzzy_score DECIMAL(5, 4),
    ranking_strategy VARCHAR(50),
    
    -- Timestamp
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_analytics_query ON search_analytics(query_text);
CREATE INDEX idx_analytics_user ON search_analytics(user_id);
CREATE INDEX idx_analytics_zero ON search_analytics(zero_results) WHERE zero_results = true;
CREATE INDEX idx_analytics_created ON search_analytics(created_at DESC);
CREATE INDEX idx_analytics_city_created ON search_analytics(city, created_at DESC);  🆕
```

---

### **Table 4: `search_popular_queries`**

**Purpose:** Cache popular searches for auto-complete

```sql
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
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    CONSTRAINT unique_popular_query 
        UNIQUE(query_text, period, COALESCE(city, ''))
);

CREATE INDEX idx_popular_period ON search_popular_queries(period, city);
CREATE INDEX idx_popular_count ON search_popular_queries(search_count DESC);
```

---

## Regional Caching Strategy 🆕

**Problem:** Geospatial queries on large datasets are expensive, even with GIST indexes.

**Solution:** Tile-based regional caching using 2km x 2km tiles

**Key Concepts:**
- Cache key format: `search:geo:{city}:{latTile}:{lonTile}`
- Tile calculation: `latTile = floor(latitude / 2)`, `lonTile = floor(longitude / 2)`
- Cache hit → Filter by exact distance within tile
- Cache miss → Query DB, cache results for 15 minutes
- Update event → Invalidate all tiles for affected city

**Cache Hierarchy:**

```
Level 1: Regional Tile Cache (15 min TTL)
  - Key: "search:geo:{city}:{latTile}:{lonTile}"
  - Reduces DB hits by 80%+

Level 2: Popular Query Cache (30 min TTL)
  - Key: "search:query:{queryHash}:{city}:{filters}"
  - Reduces DB hits for repeated searches

Level 3: Discovery Feed Cache (10 min TTL)
  - Key: "search:feed:{userId}:{latTile}:{lonTile}"
  - Personalized but tile-based
```

---

## REST API Contracts (4 Mobile-Optimized Endpoints)

### **API 1: Discovery Feed**

**Endpoint:** `GET /api/v1/search/feed`

**Request:**

```http
GET /api/v1/search/feed?latitude=12.9716&longitude=77.5946&radius=5&userId=123e4567-e89b-12d3-a456-426614174000&page=0&size=20
If-None-Match: "feed-abc123"
```

**Response (200 OK):** *(Enriched with all UI-required data)* 🆕

```json
{
  "nearbyVendors": [
    {
      "branchId": 101,
      "vendorId": 1,
      "branchName": "Chai Express - MG Road",
      "displayName": "Chai Express",
      "cuisine": ["Tea", "Snacks"],
      "rating": 4.5,
      "totalRatings": 1250,
      "deliveryTime": "20-25 min",
      "distance": 1.2,
      "distanceUnit": "km",
      "deliveryFee": 20.00,
      "minOrderValue": 50.00,
      "isOpen": true,
      "openingTime": null,
      "images": {
        "primary": "https://cdn.foodapp.com/vendors/101/cover_thumbnail.jpg",
        "cover": {
          "thumbnail": "https://cdn.foodapp.com/vendors/101/cover_thumbnail.jpg",
          "small": "https://cdn.foodapp.com/vendors/101/cover_small.jpg",
          "medium": "https://cdn.foodapp.com/vendors/101/cover_medium.jpg"
        },
        "logo": {
          "small": "https://cdn.foodapp.com/vendors/101/logo_small.png"
        }
      },
      "tags": ["Fast Delivery", "Popular"],
      "rankingScore": 0.87
    }
  ],
  "popularItems": [
    {
      "menuItemId": 501,
      "name": "Masala Chai",
      "description": "Traditional Indian spiced tea",
      "branchId": 101,
      "branchName": "Chai Express - MG Road",
      "vendorName": "Chai Express",
      "price": 20.00,
      "category": "Beverages",
      "images": {
        "primary": "https://cdn.foodapp.com/items/501/main_thumbnail.jpg",
        "medium": "https://cdn.foodapp.com/items/501/main_medium.jpg"
      },
      "rating": 4.7,
      "preparationTime": 5,
      "dietaryInfo": ["Vegetarian"],
      "isAvailable": true,
      "distance": 1.2,
      "rankingScore": 0.92
    }
  ],
  "recommendedItems": [
    {
      "menuItemId": 502,
      "name": "Samosa",
      "description": "Crispy fried pastry with spiced filling",
      "branchId": 101,
      "branchName": "Chai Express - MG Road",
      "price": 15.00,
      "category": "Snacks",
      "images": {
        "primary": "https://cdn.foodapp.com/items/502/main_thumbnail.jpg",
        "medium": "https://cdn.foodapp.com/items/502/main_medium.jpg"
      },
      "rating": 4.6,
      "preparationTime": 10,
      "dietaryInfo": ["Vegetarian", "Vegan"],
      "isAvailable": true,
      "distance": 1.2,
      "recommendationScore": 0.88
    }
  ],
  "topOrderedItems": [
    {
      "menuItemId": 503,
      "name": "Filter Coffee",
      "description": "South Indian style filtered coffee",
      "branchId": 102,
      "branchName": "Coffee House - Indiranagar",
      "price": 25.00,
      "category": "Beverages",
      "images": {
        "primary": "https://cdn.foodapp.com/items/503/main_thumbnail.jpg",
        "medium": "https://cdn.foodapp.com/items/503/main_medium.jpg"
      },
      "rating": 4.8,
      "orderCount": 2450,
      "trendingScore": 0.95,
      "preparationTime": 5,
      "isAvailable": true,
      "distance": 2.1
    }
  ],
  "searchSuggestions": ["Masala Chai", "Samosa", "Filter Coffee"],
  "metadata": {
    "totalVendors": 45,
    "cacheUntil": "2024-11-26T10:15:00Z",
    "cacheHit": true,
    "rankingVersion": "v2-blended"
  }
}
```

**4 Discovery Feed Sections:** 🆕
1. **nearbyVendors** - Location-based vendor discovery with blended ranking
2. **popularItems** - Popular items in the area (last 30 days)
3. **recommendedItems** - Personalized based on user preferences and order history
4. **topOrderedItems** - Overall trending items (last 7 days, min 10 orders)

**Response Headers:**

```
Cache-Control: max-age=600, stale-while-revalidate=1800  🆕 (10 min)
ETag: "feed-xyz789"
X-Cache-Hit: true  🆕
```

---

### **API 2: Unified Search**

**Endpoint:** `GET /api/v1/search`

**Request:**

```http
GET /api/v1/search?q=chai&type=all&latitude=12.9716&longitude=77.5946&filters={"category":["Beverages"],"priceRange":{"max":50}}&page=0&size=20
```

**Response (200 OK):** *(Enriched with all UI-required data)* 🆕

```json
{
  "query": "chai",
  "type": "all",
  "results": {
    "vendors": [
      {
        "branchId": 101,
        "branchName": "Chai Express - MG Road",
        "displayName": "Chai Express",
        "cuisine": ["Tea", "Snacks"],
        "rating": 4.5,
        "totalRatings": 1250,
        "distance": 1.2,
        "deliveryTime": "20-25 min",
        "deliveryFee": 20.00,
        "isOpen": true,
        "images": {
          "primary": "https://cdn.foodapp.com/vendors/101/cover_thumbnail.jpg",
          "cover": {
            "thumbnail": "https://cdn.foodapp.com/vendors/101/cover_thumbnail.jpg",
            "small": "https://cdn.foodapp.com/vendors/101/cover_small.jpg"
          }
        },
        "highlightedText": "<em>Chai</em> Express",
        "scores": {
          "total": 0.89,
          "fts": 0.95,
          "fuzzy": 0.85,
          "popularity": 0.78,
          "proximity": 0.92
        }
      }
    ],
    "items": [
      {
        "menuItemId": 501,
        "name": "Masala Chai",
        "description": "Traditional Indian spiced tea",
        "branchId": 101,
        "branchName": "Chai Express - MG Road",
        "vendorName": "Chai Express",
        "price": 20.00,
        "category": "Beverages",
        "images": {
          "primary": "https://cdn.foodapp.com/items/501/main_thumbnail.jpg",
          "medium": "https://cdn.foodapp.com/items/501/main_medium.jpg"
        },
        "rating": 4.7,
        "preparationTime": 5,
        "dietaryInfo": ["Vegetarian"],
        "nutrition": {
          "calories": 120,
          "servingSize": "200ml"
        },
        "isAvailable": true,
        "distance": 1.2,
        "highlightedText": "Masala <em>Chai</em>",
        "scores": {
          "total": 0.94,
          "fts": 0.98,
          "fuzzy": 0.90,
          "popularity": 0.85,
          "proximity": 0.92
        }
      }
    ]
  },
  "suggestions": ["chai latte", "masala chai"],
  "pagination": {
    "currentPage": 0,
    "totalResults": 87,
    "hasMore": true
  },
  "metadata": {
    "searchTime": "45ms",
    "cacheHit": false,
    "rankingStrategy": "blended-v2",
    "queryType": "hybrid-fts-fuzzy"
  }
}
```

---

### **API 3: Vendor Menu**

**Endpoint:** `GET /api/v1/vendors/{branchId}/menu`

**Response:** Complete menu with categories, items (with enriched images and nutrition), and recommendations (blended ranking applied)

---

### **API 4: Recommendations**

**Endpoint:** `GET /api/v1/recommendations`

**Response:** Personalized item and vendor recommendations (blended ranking applied, enriched with images and details)

---

## Image Storage & Rendering Strategy 🆕

### **Image Storage in Amazon S3**

**Bucket Structure:**
```
s3://food-app-images/
├── vendors/{vendorId}/
│   ├── cover_original.jpg      (1920x1080, ~500KB)
│   ├── cover_large.jpg         (1280x720,  ~200KB)
│   ├── cover_medium.jpg        (640x360,   ~80KB)
│   ├── cover_small.jpg         (320x180,   ~30KB)
│   ├── cover_thumbnail.jpg     (150x150,   ~10KB)
│   └── logo_small.png          (128x128,   ~15KB)
│
├── menu-items/{itemId}/
│   ├── main_original.jpg       (1920x1920, ~600KB)
│   ├── main_large.jpg          (800x800,   ~150KB)
│   ├── main_medium.jpg         (400x400,   ~50KB)
│   └── main_thumbnail.jpg      (100x100,   ~8KB)
```

**Why Multiple Image Sizes?**
- Network efficiency: Don't send 500KB for 100x100px display
- Faster loading: Smaller images = faster UI
- Data savings: Important for users on limited data plans
- Progressive loading: Show thumbnail → load high-res

**CloudFront CDN:**
- Edge caching for low latency (10-50ms for cached images)
- Custom domain: `https://cdn.foodapp.com/...`
- 24-hour TTL (configurable via Cache-Control headers)
- Automatic image optimization at edge

---

### **Image Loading Flow (Mobile App)** 🆕

```
┌─────────────────────────────────────────────────────────────┐
│                     Mobile App                               │
└─────────────────────────────────────────────────────────────┘
                              │
                              │ 1. API Request (Discovery Feed / Search)
                              ▼
┌─────────────────────────────────────────────────────────────┐
│              Order-Catalog-Service API                       │
│  Returns: JSON with multiple image URLs for each size       │
│  {                                                            │
│    "images": {                                               │
│      "primary": "https://cdn.foodapp.com/.../thumbnail.jpg",│
│      "cover": {                                              │
│        "thumbnail": "...thumbnail.jpg",                      │
│        "small": "...small.jpg",                              │
│        "medium": "...medium.jpg"                             │
│      }                                                        │
│    }                                                          │
│  }                                                            │
└─────────────────────────────────────────────────────────────┘
                              │
                              │ 2. Parse JSON & Select Optimal Size
                              ▼
┌─────────────────────────────────────────────────────────────┐
│          Mobile App - Image Selection Logic                 │
│  • List/Grid: Use thumbnail (150x150, ~10KB)                │
│  • Detail screen: Use medium/large (progressive loading)    │
│  • Full screen: Use original                                 │
│  • Consider: network type, display size, user preference    │
└─────────────────────────────────────────────────────────────┘
                              │
                              │ 3. Image Request
                              ▼
┌─────────────────────────────────────────────────────────────┐
│         Glide/Coil/Kingfisher Image Loader                  │
│  Check Memory Cache → Hit? ✓ Display immediately            │
│                    → Miss? Continue...                       │
└─────────────────────────────────────────────────────────────┘
                              │
                              │ 4. Check Disk Cache
                              ▼
┌─────────────────────────────────────────────────────────────┐
│              Device Disk Cache (100-250 MB)                 │
│  Hit? ✓ Decode → Memory Cache → Display                     │
│  Miss? Continue...                                           │
└─────────────────────────────────────────────────────────────┘
                              │
                              │ 5. Network Request
                              ▼
┌─────────────────────────────────────────────────────────────┐
│          CloudFront CDN (Edge Location)                     │
│  Hit? ✓ Return cached image (10-50ms)                       │
│  Miss? Fetch from S3...                                      │
└─────────────────────────────────────────────────────────────┘
                              │
                              │ 6. Origin Fetch (if CDN miss)
                              ▼
┌─────────────────────────────────────────────────────────────┐
│              Amazon S3 Bucket                                │
│  Return image file                                           │
│  CloudFront caches for next request                         │
└─────────────────────────────────────────────────────────────┘
                              │
                              │ 7. Response
                              ▼
┌─────────────────────────────────────────────────────────────┐
│              Mobile App                                      │
│  • Save to disk cache (7-30 days TTL)                        │
│  • Decode image                                              │
│  • Save to memory cache (until app killed)                   │
│  • Display on screen                                         │
└─────────────────────────────────────────────────────────────┘
```

**Image Rendering Strategy by Screen:**

| Screen | Image Size | Resolution | File Size | Strategy |
|--------|-----------|------------|-----------|----------|
| Discovery Feed List | Thumbnail | 150x150 | ~10KB | Immediate load |
| Search Grid | Small | 320x320 | ~30KB | Lazy load |
| Vendor Detail Banner | Medium/Large | 640x360 | ~80KB | Progressive (thumb → high-res) |
| Image Gallery | Original | 1920x1920 | ~600KB | On-demand, pinch-to-zoom |

**Performance Optimization:**
- **Lazy Loading**: Only load images when they enter viewport
- **Prefetching**: Prefetch next page in background
- **Downsampling**: Decode only required resolution
- **Progressive Loading**: Show blurred thumbnail → fade in high-res
- **Caching**: 3-level cache (Memory → Disk → CDN)

---

## Implementation Strategy (Revised)

### **Phase 1: Database Setup**

1. ✅ Create migration `V10__Create_search_tables.sql`
2. ✅ Add search tables with indexes
3. ✅ Enable `pg_trgm` and `postgis` extensions
4. ✅ Add size constraints on arrays (cuisine, tags)
5. ✅ Add `sync_version` and `normalized_popularity` columns

### **Phase 2: Event Infrastructure (AWS SNS/SQS)** 🆕

1. ✅ Create SNS topics: `vendor-index-events`, `menu-index-events`
2. ✅ Create SQS queues: `search-index-queue`, `search-index-dlq` (Dead Letter Queue)
3. ✅ Subscribe SQS queue to SNS topics (fanout pattern)
4. ✅ Implement `VendorIndexEvent` and `MenuItemIndexEvent` DTOs
5. ✅ Implement `SearchEventPublisher` (SNS publisher)
6. ✅ Configure visibility timeout (30s) and max receive count (3)

### **Phase 3: Domain Models**

1. ✅ Create JPA entities: `SearchVendor`, `SearchMenuItem`
2. ✅ Create repositories with custom queries
3. ✅ Add PostGIS and full-text search support
4. ✅ Add `@PrePersist` validation for array sizes

### **Phase 4: Sync Services (Event-Driven via SQS)** 🆕

1. ✅ `SearchEventConsumer` - SQS poller for index events (long polling 20s)
2. ✅ `VendorSearchIndexService` - Sync logic for vendors
3. ✅ `MenuItemSearchIndexService` - Sync logic for menu items
4. ✅ Bulk initialization on startup (read from transactional tables)
5. ✅ Incremental updates via SNS/SQS events
6. ✅ Data validation before sync (array size limits, image JSONB)

### **Phase 5: Search Services with Blended Ranking** 🆕

1. ✅ `SearchRankingService` - Configurable weighted scoring
   - FTS weight: 0.50
   - Fuzzy weight: 0.30
   - Popularity weight: 0.15
   - Proximity weight: 0.05

2. ✅ `DiscoveryFeedService` - Aggregates nearby vendors, popular items
   - Uses filter-first geospatial queries
   - Applies blended ranking to results

3. ✅ `UnifiedSearchService` - Full-text + fuzzy + geospatial search
   - Parallel FTS and fuzzy execution
   - Blended scoring for all results
   - Continuous ranking (no binary fallback)

4. ✅ `VendorMenuService` - Complete menu with recommendations

5. ✅ `RecommendationService` - Blended ranking for recommendations

### **Phase 6: Caching Layer (Regional)** 🆕

1. ✅ `SearchCacheService` - Tile-based regional caching
   - 2km x 2km tiles
   - 15 min TTL for geo cache
   - 30 min TTL for query cache
   - 10 min TTL for feed cache

2. ✅ ETag generation for HTTP caching

3. ✅ Cache invalidation on updates (tile-based)

4. ✅ Cache hit/miss tracking in analytics

### **Phase 7: REST Controllers**

1. ✅ `SearchController` - 4 endpoints with proper DTOs
2. ✅ HTTP caching headers (ETag, Cache-Control)
3. ✅ OpenAPI documentation
4. ✅ Expose ranking scores in responses (for debugging)

### **Phase 8: Analytics & Monitoring** 🆕

1. ✅ `SearchAnalyticsService` - Track queries with ranking metrics
   - avg_fts_score, avg_fuzzy_score
   - ranking_strategy used

2. ✅ Scheduled job to populate popular queries

3. ✅ Performance monitoring
   - Cache hit rates
   - Query execution times
   - Ranking score distributions

4. ✅ DLQ monitoring and alerting

---

## Scenarios Covered (Phase 1)

✅ Location-based vendor discovery (filter-first strategy)

✅ Full-text search on vendors and menu items (enriched responses)

✅ Fuzzy matching for typos (pg_trgm)

✅ **Blended ranking** combining FTS, fuzzy, popularity, and proximity 🆕

✅ Multi-criteria filtering (price, category, rating, availability)

✅ Auto-complete suggestions

✅ **4-section Discovery Feed** (Nearby, Popular, Recommended, Trending) 🆕

✅ Advanced recommendations (blended ranking, personalized)

✅ HTTP caching (ETag, 304)

✅ **Regional tile-based Redis caching** 🆕

✅ **Event-driven data synchronization (AWS SNS/SQS)** 🆕

✅ **Data quality controls (array size limits)** 🆕

✅ **Multiple image sizes in JSONB (S3 + CloudFront)** 🆕

✅ **Progressive image loading strategy** 🆕

✅ Search analytics tracking with ranking metrics

✅ **DLQ for failed sync events with CloudWatch alarms** 🆕

---

## Pending for Phase 2

🔄 Advanced ML-based recommendations

🔄 Natural language search

🔄 Voice search integration

🔄 Collaborative filtering

🔄 A/B testing framework for ranking weights

🔄 Advanced analytics dashboard

🔄 Auto-tuning of ranking weights based on CTR

---

## Success Metrics

### Performance Metrics
- Search response time < 200ms (95th percentile)
- Feed API < 300ms
- Cache hit rate > 70% 🆕 (target: 80%+)
- Search result relevance > 80%
- API availability > 99.9%

### Ranking Quality Metrics 🆕
- Top-3 Click-Through Rate (CTR) > 60%
- Position-weighted CTR (pCTR) > 0.40
- Zero-result rate < 5%
- Average ranking score > 0.70

### System Health Metrics 🆕
- Event processing lag < 5 seconds (p95)
- DLQ accumulation < 10 events/hour
- Sync success rate > 99.5%
- Cache invalidation latency < 1 second

---

## Configuration (application.yml)

```yaml
# AWS Configuration
aws:
  region: ap-south-1
  sns:
    topics:
      vendor-index: arn:aws:sns:ap-south-1:123456789:vendor-index-events
      menu-index: arn:aws:sns:ap-south-1:123456789:menu-index-events
  sqs:
    queues:
      search-index:
        url: https://sqs.ap-south-1.amazonaws.com/123456789/search-index-queue
        visibility-timeout: 30      # seconds
        wait-time: 20               # long polling
        max-messages: 10            # batch size
        max-receive-count: 3        # before DLQ
      search-index-dlq:
        url: https://sqs.ap-south-1.amazonaws.com/123456789/search-index-dlq
  s3:
    bucket:
      images: food-app-images
    cloudfront:
      domain: cdn.foodapp.com

# Search Configuration
search:
  ranking:
    weights:
      fts: 0.50
      fuzzy: 0.30
      popularity: 0.15
      proximity: 0.05
    fuzzy:
      similarity-threshold: 0.3
  
  cache:
    tile-size-km: 2
    ttl:
      geo-tile: 900        # 15 minutes
      query-cache: 1800    # 30 minutes
      feed-cache: 600      # 10 minutes
  
  data-quality:
    max-cuisine-count: 20
    max-tags-count: 20
    max-dietary-info-count: 10
  
  geospatial:
    default-radius-km: 5
    max-radius-km: 20
  
  images:
    sizes:
      - original
      - large
      - medium
      - small
      - thumbnail
```

---

## Risk Mitigation Summary

### 1. Data Inconsistency Risk
**Mitigation:** Event-driven sync with retry + DLQ

### 2. Performance Degradation Risk
**Mitigation:** Filter-first geospatial queries + regional caching

### 3. Relevance Quality Risk
**Mitigation:** Blended ranking with configurable weights + A/B testing (Phase 2)

### 4. High Cardinality Risk
**Mitigation:** Array size constraints + validation before sync

### 5. Cache Invalidation Risk
**Mitigation:** Tile-based invalidation (granular, not city-wide)

---

## Testing Strategy

### Unit Tests
- ✅ Blended ranking calculation
- ✅ Geospatial distance calculations
- ✅ Array size validation
- ✅ Cache key generation

### Integration Tests
- ✅ SNS/SQS event publishing and consumption (using LocalStack)
- ✅ Database queries with PostGIS (geospatial operations)
- ✅ Redis caching operations (tile-based caching)
- ✅ Full-text and fuzzy search (pg_trgm)
- ✅ Image JSONB storage and retrieval
- ✅ S3 image upload and CloudFront access (using LocalStack)

### Performance Tests
- ✅ Search query latency (p50, p95, p99)
- ✅ Cache hit rate measurement
- ✅ Concurrent search requests
- ✅ Event processing throughput

### End-to-End Tests
- ✅ Vendor update → SNS → SQS → Search index sync → Cache invalidation
- ✅ Search with typos → Fuzzy results with enriched data
- ✅ Location-based search → Blended ranking → 4 feed sections
- ✅ Cache hit → ETag validation → 304 response
- ✅ Image upload → S3 → CloudFront → API response → Mobile rendering
- ✅ DLQ flow: Failed sync → Retry → DLQ → CloudWatch alarm

---

## Implementation Todos

### Phase 1: Foundation (Week 1-2)
- [ ] Create database migration with search tables, indexes, and extensions
- [ ] Add JSONB columns for images storage (multiple sizes)
- [ ] Add columns for trending/recommendations (order_count_7d, trending_score)
- [ ] Create AWS SNS topics (`vendor-index-events`, `menu-index-events`)
- [ ] Create AWS SQS queues (`search-index-queue`, `search-index-dlq`)
- [ ] Subscribe SQS to SNS (fanout pattern)
- [ ] Set up S3 bucket for images with CloudFront CDN
- [ ] Implement event DTOs (`VendorIndexEvent`, `MenuItemIndexEvent`)
- [ ] Create JPA entities (`SearchVendor`, `SearchMenuItem`) with validation

### Phase 2: Event Infrastructure (Week 2-3)
- [ ] Implement `SearchEventPublisher` (AWS SNS publisher)
- [ ] Implement `SearchEventConsumer` (SQS poller with long polling)
- [ ] Implement `VendorSearchIndexService` (sync logic with image JSONB)
- [ ] Implement `MenuItemSearchIndexService` (sync logic with image JSONB)
- [ ] Add CloudWatch alarms for DLQ monitoring

### Phase 3: Search Core (Week 3-4)
- [ ] Implement `SearchRankingService` with blended scoring algorithm
- [ ] Implement `UnifiedSearchService` with hybrid FTS+Fuzzy queries
- [ ] Implement `DiscoveryFeedService` with 4 sections:
  - [ ] Nearby Vendors (filter-first geospatial)
  - [ ] Popular Items (last 30 days)
  - [ ] Recommended Items (personalized)
  - [ ] Top Ordered Items (trending, last 7 days)
- [ ] Implement `VendorMenuService` with enriched responses
- [ ] Implement `RecommendationService` with blended ranking

### Phase 4: Caching (Week 4-5)
- [ ] Implement `SearchCacheService` with tile-based regional caching
- [ ] Implement cache invalidation on updates (tile-based)
- [ ] Add ETag generation and validation
- [ ] Implement cache hit/miss tracking

### Phase 5: REST APIs (Week 5-6)
- [ ] Implement `SearchController` with 4 endpoints
- [ ] Add enriched response DTOs:
  - [ ] Multiple image sizes (JSONB structure)
  - [ ] Complete vendor/item details (rating, nutrition, prep time, etc.)
  - [ ] Ranking scores (for debugging)
- [ ] Add HTTP caching headers (ETag, Cache-Control)
- [ ] Add OpenAPI documentation
- [ ] Image URL helper methods (select optimal size based on context)

### Phase 6: Analytics & Monitoring (Week 6-7)
- [ ] Implement `SearchAnalyticsService` with ranking metrics
- [ ] Create scheduled job for popular queries aggregation
- [ ] Add performance monitoring dashboards
- [ ] Add DLQ monitoring and alerts

### Phase 7: Testing & Optimization (Week 7-8)
- [ ] Write unit tests for all services
- [ ] Write integration tests for SNS/SQS and database (using LocalStack)
- [ ] Performance testing and query optimization
- [ ] End-to-end testing of complete flows (including image loading)
- [ ] Load testing and tuning
- [ ] Test image rendering on mobile (Android & iOS)

### Phase 8: Deployment (Week 8)
- [ ] Provision AWS resources:
  - [ ] SNS topics
  - [ ] SQS queues with DLQ
  - [ ] S3 bucket for images
  - [ ] CloudFront CDN distribution
  - [ ] CloudWatch alarms
- [ ] Update build.gradle with AWS SDK dependencies
- [ ] Configuration management for different environments (dev, staging, prod)
- [ ] IAM roles and policies for SNS/SQS/S3 access
- [ ] Deployment documentation and runbooks
- [ ] Image upload pipeline and optimization

---

## Dependencies

### Build Dependencies (build.gradle)

```gradle
dependencies {
    // Existing dependencies...
    
    // PostGIS for geospatial queries
    implementation 'org.hibernate:hibernate-spatial:6.2.7.Final'
    implementation 'org.postgresql:postgresql:42.6.0'
    implementation 'net.postgis:postgis-jdbc:2023.1.0'
    
    // AWS SDK for SNS/SQS (event-driven sync)
    implementation platform('software.amazon.awssdk:bom:2.20.0')
    implementation 'software.amazon.awssdk:sns'
    implementation 'software.amazon.awssdk:sqs'
    implementation 'software.amazon.awssdk:s3'
    
    // Spring Cloud AWS (optional, for easier integration)
    implementation 'io.awspring.cloud:spring-cloud-aws-starter-sqs:3.0.0'
    
    // Redis for caching
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    
    // Retry support
    implementation 'org.springframework.retry:spring-retry'
    implementation 'org.springframework:spring-aspects'
    
    // Testing
    testImplementation 'org.testcontainers:postgresql:1.19.1'
    testImplementation 'org.testcontainers:localstack:1.19.1'  // For AWS testing
}
```

---

## Summary of Key Improvements 🎯

| Aspect | Original Plan | Revised Plan |
|--------|--------------|--------------|
| **Ranking** | Binary FTS-or-Fuzzy | Blended weighted scoring (4 factors) |
| **Sync** | Direct DB update | Event-driven (AWS SNS/SQS + retry + DLQ) |
| **Geospatial** | Standard queries | Filter-first + tile-based caching |
| **Data Quality** | None | Array size limits + validation |
| **Caching** | Basic Redis | Regional tile-based (3 levels) |
| **Monitoring** | Basic analytics | Ranking metrics + CloudWatch alerts |
| **Consistency** | At-risk | Guaranteed (event sourcing) |
| **Images** | Single URL | Multiple sizes in JSONB (S3 + CloudFront) |
| **API Response** | Minimal data | Enriched with all UI-required details |
| **Feed Sections** | 2 sections | 4 sections (added Recommended & Trending) |

---

## Next Steps

1. **Review and Approve**: Stakeholder sign-off on revised architecture
2. **AWS Infrastructure Setup**:
   - Provision SNS topics and SQS queues with DLQ
   - Set up S3 bucket for images with CloudFront CDN
   - Configure CloudWatch alarms for monitoring
   - Set up IAM roles and policies
3. **Prototype**: Build proof-of-concept for:
   - Blended ranking algorithm
   - Image storage and retrieval (multiple sizes)
   - SNS/SQS event flow
4. **Phased Implementation**: Follow 8-week implementation plan
5. **Mobile Integration**: Coordinate with mobile team for image rendering
6. **A/B Testing** (Phase 2): Compare ranking strategies and tune weights

---

**Ready to proceed with implementation?** 🚀

