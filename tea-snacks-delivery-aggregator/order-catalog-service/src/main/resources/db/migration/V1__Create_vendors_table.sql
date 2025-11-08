-- V1__Create_vendors_table.sql
-- Vendors table for company/brand level information

CREATE TABLE vendors (
    vendor_id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    
    -- Company details
    company_name VARCHAR(255) NOT NULL,
    brand_name VARCHAR(255),
    legal_entity_name VARCHAR(255),
    
    -- Contact
    company_email VARCHAR(255),
    company_phone VARCHAR(20),
    
    -- Legal/Tax
    pan_number VARCHAR(20),
    gst_number VARCHAR(20),
    
    -- Company-level images (JSONB)
    images JSONB DEFAULT '{
        "logo": null,
        "cover_photo": null,
        "brand_assets": []
    }'::jsonb,
    
    -- Flexible metadata
    metadata JSONB DEFAULT '{}'::jsonb,
    
    -- Tags
    tags TEXT[] DEFAULT ARRAY[]::TEXT[],
    
    -- Audit
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT unique_company_email UNIQUE(company_email)
);

-- Indexes
CREATE INDEX idx_vendors_user_id ON vendors(user_id);
CREATE INDEX idx_vendors_company_name ON vendors(company_name);
CREATE INDEX idx_vendors_tags ON vendors USING GIN(tags);
