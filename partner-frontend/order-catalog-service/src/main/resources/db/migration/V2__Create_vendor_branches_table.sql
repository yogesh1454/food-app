-- V2__Create_vendor_branches_table.sql
-- Vendor branches table for location level information

CREATE TABLE vendor_branches (
    branch_id BIGSERIAL PRIMARY KEY,
    vendor_id BIGINT NOT NULL REFERENCES vendors(vendor_id) ON DELETE CASCADE,
    
    -- Branch identification
    branch_name VARCHAR(255) NOT NULL,
    branch_code VARCHAR(50) UNIQUE,
    display_name VARCHAR(255),
    
    -- Location
    address JSONB NOT NULL DEFAULT '{
        "street": "",
        "landmark": "",
        "area": "",
        "city": "",
        "state": "",
        "pincode": "",
        "country": "India"
    }'::jsonb,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    city VARCHAR(100) NOT NULL,
    
    -- Contact
    branch_phone VARCHAR(20),
    branch_email VARCHAR(255),
    branch_manager_name VARCHAR(255),
    branch_manager_phone VARCHAR(20),
    
    -- Onboarding & Verification
    onboarding_status VARCHAR(50) DEFAULT 'PENDING',
    verification_notes TEXT,
    verified_at TIMESTAMP WITH TIME ZONE,
    verified_by UUID,
    
    -- Branch Preferences (JSONB)
    preferences JSONB DEFAULT '{
        "auto_accept_orders": false,
        "max_orders_per_hour": 50,
        "delivery_radius_km": 5,
        "min_order_value": 0,
        "accepts_cash": true,
        "accepts_online_payment": true,
        "packing_time_minutes": 10,
        "commission_rate": 15.0,
        "priority_delivery": false
    }'::jsonb,
    
    -- Branch images (JSONB)
    images JSONB DEFAULT '{
        "logo": null,
        "cover_photo": null,
        "storefront": null,
        "interior": [],
        "kitchen": [],
        "gallery": []
    }'::jsonb,
    
    -- Operations
    is_active BOOLEAN DEFAULT FALSE,
    is_open BOOLEAN DEFAULT FALSE,
    operating_hours JSONB DEFAULT '{
        "MONDAY": [{"open": "09:00", "close": "21:00"}],
        "TUESDAY": [{"open": "09:00", "close": "21:00"}],
        "WEDNESDAY": [{"open": "09:00", "close": "21:00"}],
        "THURSDAY": [{"open": "09:00", "close": "21:00"}],
        "FRIDAY": [{"open": "09:00", "close": "21:00"}],
        "SATURDAY": [{"open": "09:00", "close": "21:00"}],
        "SUNDAY": [{"open": "09:00", "close": "21:00"}]
    }'::jsonb,
    
    -- Performance metrics
    rating DECIMAL(3,2) DEFAULT 0.00,
    total_orders INTEGER DEFAULT 0,
    total_reviews INTEGER DEFAULT 0,
    
    -- Menu versioning
    menu_version INTEGER DEFAULT 1,
    
    -- Flexible metadata
    metadata JSONB DEFAULT '{}'::jsonb,
    
    -- Tags
    tags TEXT[] DEFAULT ARRAY[]::TEXT[],
    
    -- Audit
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    activated_at TIMESTAMP WITH TIME ZONE
);

-- Indexes
CREATE INDEX idx_branches_vendor_id ON vendor_branches(vendor_id);
CREATE INDEX idx_branches_city ON vendor_branches(city);
CREATE INDEX idx_branches_onboarding_status ON vendor_branches(onboarding_status);
CREATE INDEX idx_branches_active ON vendor_branches(is_active, is_open);
CREATE INDEX idx_branches_rating ON vendor_branches(rating DESC);
CREATE INDEX idx_branches_tags ON vendor_branches USING GIN(tags);
CREATE INDEX idx_branches_preferences ON vendor_branches USING GIN(preferences);
