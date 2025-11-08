-- Migration: Create Guest Users Table
-- Description: Creates table for storing guest user information and session management
-- Version: V3
-- Date: 2024-01-XX

-- Create table only if it doesn't exist
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'guest_users') THEN
        CREATE TABLE guest_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id VARCHAR(64) NOT NULL UNIQUE,
    user_agent TEXT,
    ip_address VARCHAR(45),
    platform VARCHAR(20),
    version VARCHAR(20),
    session_token VARCHAR(36) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    last_activity_at TIMESTAMP,
    action_count INTEGER NOT NULL DEFAULT 0,
    conversion_prompts_shown INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    converted_to_user_id UUID NULL,
    converted_at TIMESTAMP NULL,
            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP NULL
        );
    END IF;
END $$;

-- Create indexes for performance (only if they don't exist)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT FROM pg_indexes WHERE indexname = 'idx_guest_users_device_id') THEN
        CREATE INDEX idx_guest_users_device_id ON guest_users(device_id);
    END IF;
    
    IF NOT EXISTS (SELECT FROM pg_indexes WHERE indexname = 'idx_guest_users_session_token') THEN
        CREATE INDEX idx_guest_users_session_token ON guest_users(session_token);
    END IF;
    
    IF NOT EXISTS (SELECT FROM pg_indexes WHERE indexname = 'idx_guest_users_expires_at') THEN
        CREATE INDEX idx_guest_users_expires_at ON guest_users(expires_at);
    END IF;
    
    IF NOT EXISTS (SELECT FROM pg_indexes WHERE indexname = 'idx_guest_users_created_at') THEN
        CREATE INDEX idx_guest_users_created_at ON guest_users(created_at);
    END IF;
    
    IF NOT EXISTS (SELECT FROM pg_indexes WHERE indexname = 'idx_guest_users_is_active') THEN
        CREATE INDEX idx_guest_users_is_active ON guest_users(is_active);
    END IF;
    
    IF NOT EXISTS (SELECT FROM pg_indexes WHERE indexname = 'idx_guest_users_action_count') THEN
        CREATE INDEX idx_guest_users_action_count ON guest_users(action_count);
    END IF;

    -- Create index for finding expired sessions
    IF NOT EXISTS (SELECT FROM pg_indexes WHERE indexname = 'idx_guest_users_expired_sessions') THEN
        CREATE INDEX idx_guest_users_expired_sessions ON guest_users(expires_at, is_active) 
        WHERE is_active = TRUE;
    END IF;

    -- Create index for finding sessions needing conversion prompts
    IF NOT EXISTS (SELECT FROM pg_indexes WHERE indexname = 'idx_guest_users_conversion_prompts') THEN
        CREATE INDEX idx_guest_users_conversion_prompts ON guest_users(action_count, is_active, expires_at) 
        WHERE is_active = TRUE;
    END IF;
END $$;

-- Add comments for documentation (only if table exists)
DO $$ 
BEGIN
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'guest_users') THEN
        COMMENT ON TABLE guest_users IS 'Stores guest user information and session management data';
        COMMENT ON COLUMN guest_users.id IS 'Unique guest user identifier';
        COMMENT ON COLUMN guest_users.device_id IS 'Device fingerprint identifier';
        COMMENT ON COLUMN guest_users.user_agent IS 'User agent string from browser/client';
        COMMENT ON COLUMN guest_users.ip_address IS 'IP address of the guest user';
        COMMENT ON COLUMN guest_users.platform IS 'Platform type (web|ios|android)';
        COMMENT ON COLUMN guest_users.version IS 'Platform version';
        COMMENT ON COLUMN guest_users.session_token IS 'Unique session token for guest user';
        COMMENT ON COLUMN guest_users.expires_at IS 'Session expiration timestamp';
        COMMENT ON COLUMN guest_users.last_activity_at IS 'Last activity timestamp';
        COMMENT ON COLUMN guest_users.action_count IS 'Number of actions performed by guest user';
        COMMENT ON COLUMN guest_users.conversion_prompts_shown IS 'Number of conversion prompts shown';
        COMMENT ON COLUMN guest_users.is_active IS 'Whether guest session is active';
        COMMENT ON COLUMN guest_users.converted_to_user_id IS 'ID of registered user if converted';
        COMMENT ON COLUMN guest_users.converted_at IS 'Timestamp when guest was converted to registered user';
        COMMENT ON COLUMN guest_users.created_at IS 'Timestamp when guest user was created';
        COMMENT ON COLUMN guest_users.updated_at IS 'Timestamp when guest user was last updated';
    END IF;
END $$; 