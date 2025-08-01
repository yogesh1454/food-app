-- V1__create_user_tables.sql
-- Create users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE,
    phone_number VARCHAR(20) UNIQUE,
    password_hash VARCHAR(255),
    name VARCHAR(100) NOT NULL,
    user_type VARCHAR(20) NOT NULL CHECK (user_type IN ('REGISTERED', 'GUEST')),
    status VARCHAR(30) NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING_VERIFICATION')),
    role VARCHAR(20) NOT NULL CHECK (role IN ('CUSTOMER', 'VENDOR', 'DELIVERY_PARTNER', 'ADMIN')),
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    phone_verified BOOLEAN NOT NULL DEFAULT FALSE,
    profile_completion_percentage INTEGER DEFAULT 0,
    last_login_at TIMESTAMP,
    device_id VARCHAR(255),
    user_agent VARCHAR(500),
    google_id VARCHAR(255) UNIQUE,
    facebook_id VARCHAR(255) UNIQUE,
    instagram_id VARCHAR(255) UNIQUE,
    twitter_id VARCHAR(255) UNIQUE,
    converted_from_guest_id UUID,
    conversion_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Create user_profiles table
CREATE TABLE user_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    date_of_birth DATE,
    gender VARCHAR(20) CHECK (gender IN ('MALE', 'FEMALE', 'OTHER', 'PREFER_NOT_TO_SAY')),
    avatar_url VARCHAR(500),
    bio VARCHAR(500),
    address_line_1 VARCHAR(255),
    address_line_2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    preferred_language VARCHAR(10) DEFAULT 'en',
    timezone VARCHAR(50),
    notification_preferences TEXT,
    business_name VARCHAR(255),
    business_type VARCHAR(100),
    business_registration_number VARCHAR(100),
    gst_number VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Create indexes for performance
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_phone ON users(phone_number);
CREATE INDEX idx_user_status ON users(status);
CREATE INDEX idx_user_type ON users(user_type);
CREATE INDEX idx_user_role ON users(role);
CREATE INDEX idx_user_created_at ON users(created_at);
CREATE INDEX idx_user_last_login ON users(last_login_at);

-- Social login indexes
CREATE INDEX idx_user_google_id ON users(google_id) WHERE google_id IS NOT NULL;
CREATE INDEX idx_user_facebook_id ON users(facebook_id) WHERE facebook_id IS NOT NULL;
CREATE INDEX idx_user_instagram_id ON users(instagram_id) WHERE instagram_id IS NOT NULL;
CREATE INDEX idx_user_twitter_id ON users(twitter_id) WHERE twitter_id IS NOT NULL;

-- Guest conversion indexes
CREATE INDEX idx_user_device_id ON users(device_id) WHERE device_id IS NOT NULL;
CREATE INDEX idx_user_converted_from_guest ON users(converted_from_guest_id) WHERE converted_from_guest_id IS NOT NULL;

-- User profile indexes
CREATE INDEX idx_profile_user_id ON user_profiles(user_id);
CREATE INDEX idx_profile_city ON user_profiles(city);
CREATE INDEX idx_profile_state ON user_profiles(state);
CREATE INDEX idx_profile_country ON user_profiles(country);
CREATE INDEX idx_profile_business_name ON user_profiles(business_name) WHERE business_name IS NOT NULL;
CREATE INDEX idx_profile_business_type ON user_profiles(business_type) WHERE business_type IS NOT NULL;
CREATE INDEX idx_profile_gst_number ON user_profiles(gst_number) WHERE gst_number IS NOT NULL;

-- Spatial index for location-based queries (requires PostGIS extension)
-- TODO: Enable when PostGIS is installed
-- CREATE INDEX idx_profile_location ON user_profiles USING GIST (
--     ll_to_earth(latitude, longitude)
-- ) WHERE latitude IS NOT NULL AND longitude IS NOT NULL;

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Triggers to automatically update updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_profiles_updated_at BEFORE UPDATE ON user_profiles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Create table for OTP verification
CREATE TABLE otp_verifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    phone_number VARCHAR(20) NOT NULL,
    otp_code VARCHAR(6) NOT NULL,
    session_id VARCHAR(255) NOT NULL UNIQUE,
    attempts INTEGER DEFAULT 0,
    verified BOOLEAN DEFAULT FALSE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_otp_phone_number ON otp_verifications(phone_number);
CREATE INDEX idx_otp_session_id ON otp_verifications(session_id);
CREATE INDEX idx_otp_expires_at ON otp_verifications(expires_at);

-- Create table for refresh tokens
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    device_id VARCHAR(255),
    user_agent VARCHAR(500),
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP
);

CREATE INDEX idx_refresh_token_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_token_hash ON refresh_tokens(token_hash);
CREATE INDEX idx_refresh_token_expires_at ON refresh_tokens(expires_at);
CREATE INDEX idx_refresh_token_device_id ON refresh_tokens(device_id) WHERE device_id IS NOT NULL;
