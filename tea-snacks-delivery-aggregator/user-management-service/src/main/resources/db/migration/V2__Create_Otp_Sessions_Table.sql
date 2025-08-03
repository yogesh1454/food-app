-- Migration: Create OTP Sessions Table
-- Description: Creates table for storing OTP session information for phone registration
-- Version: V2
-- Date: 2024-01-XX

CREATE TABLE otp_sessions (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(36) NOT NULL UNIQUE,
    phone_number VARCHAR(20) NOT NULL,
    otp VARCHAR(6) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    attempts_remaining INTEGER NOT NULL DEFAULT 5,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL
);

-- Create indexes for performance
CREATE INDEX idx_otp_sessions_session_id ON otp_sessions(session_id);
CREATE INDEX idx_otp_sessions_phone_number ON otp_sessions(phone_number);
CREATE INDEX idx_otp_sessions_expires_at ON otp_sessions(expires_at);
CREATE INDEX idx_otp_sessions_created_at ON otp_sessions(created_at);

-- Create index for rate limiting queries
CREATE INDEX idx_otp_sessions_phone_created ON otp_sessions(phone_number, created_at);

-- Add comments for documentation
COMMENT ON TABLE otp_sessions IS 'Stores OTP session information for phone number registration';
COMMENT ON COLUMN otp_sessions.session_id IS 'Unique session identifier for OTP verification';
COMMENT ON COLUMN otp_sessions.phone_number IS 'Phone number in international format';
COMMENT ON COLUMN otp_sessions.otp IS '6-digit OTP code';
COMMENT ON COLUMN otp_sessions.expires_at IS 'OTP expiration timestamp';
COMMENT ON COLUMN otp_sessions.attempts_remaining IS 'Number of verification attempts remaining';
COMMENT ON COLUMN otp_sessions.used IS 'Whether OTP has been used for registration';
COMMENT ON COLUMN otp_sessions.used_at IS 'Timestamp when OTP was used';
COMMENT ON COLUMN otp_sessions.created_at IS 'Timestamp when OTP session was created';
COMMENT ON COLUMN otp_sessions.updated_at IS 'Timestamp when OTP session was last updated'; 