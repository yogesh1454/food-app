-- Notification Service Database Schema
-- Version: 1.0
-- Description: Creates tables for notification logging and rate limiting

-- Create notification_logs table
CREATE TABLE notification_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID,
    type VARCHAR(20) NOT NULL,
    template VARCHAR(50) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    provider_response TEXT,
    provider_message_id VARCHAR(255),
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    failed_at TIMESTAMP,
    retry_count INTEGER NOT NULL DEFAULT 0,
    error_message TEXT,
    priority VARCHAR(10) DEFAULT 'NORMAL',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Create indexes for notification_logs
CREATE INDEX idx_notification_logs_user_id ON notification_logs(user_id);
CREATE INDEX idx_notification_logs_type ON notification_logs(type);
CREATE INDEX idx_notification_logs_status ON notification_logs(status);
CREATE INDEX idx_notification_logs_created_at ON notification_logs(created_at);
CREATE INDEX idx_notification_logs_recipient ON notification_logs(recipient);
CREATE INDEX idx_notification_logs_template ON notification_logs(template);

-- Create notification_rate_limits table
CREATE TABLE notification_rate_limits (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    identifier VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,
    count INTEGER NOT NULL DEFAULT 1,
    window_start TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for notification_rate_limits
CREATE INDEX idx_rate_limits_identifier_type ON notification_rate_limits(identifier, type);
CREATE INDEX idx_rate_limits_window_start ON notification_rate_limits(window_start);

-- Create unique constraint for rate limiting
CREATE UNIQUE INDEX idx_rate_limits_unique ON notification_rate_limits(identifier, type, window_start);

-- Add comments for documentation
COMMENT ON TABLE notification_logs IS 'Stores all notification attempts and their status';
COMMENT ON TABLE notification_rate_limits IS 'Tracks rate limiting for notifications per identifier';

COMMENT ON COLUMN notification_logs.type IS 'Type of notification: EMAIL, SMS, PUSH, IN_APP';
COMMENT ON COLUMN notification_logs.status IS 'Status: PENDING, PROCESSING, SENT, DELIVERED, FAILED, CANCELLED';
COMMENT ON COLUMN notification_logs.priority IS 'Priority: LOW, NORMAL, HIGH, URGENT';
COMMENT ON COLUMN notification_rate_limits.identifier IS 'Email address or phone number';
COMMENT ON COLUMN notification_rate_limits.window_start IS 'Start of the rate limiting window';
