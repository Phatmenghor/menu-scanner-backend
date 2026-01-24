-- =====================================================
-- Migration V4: Create User Sessions Table
-- Purpose: Track user login sessions and devices (like Facebook)
-- Date: 2026-01-24
-- =====================================================

CREATE TABLE user_sessions (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- User Reference
    user_id UUID NOT NULL,
    refresh_token_id UUID,

    -- Device Information
    device_id VARCHAR(255),
    device_name VARCHAR(255),
    device_type VARCHAR(50), -- WEB, MOBILE, TABLET, DESKTOP
    user_agent VARCHAR(500),
    browser VARCHAR(100),
    operating_system VARCHAR(100),

    -- Location Information (like Facebook)
    ip_address VARCHAR(45),
    country VARCHAR(100),
    city VARCHAR(100),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),

    -- Session Status
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, EXPIRED, REVOKED, LOGGED_OUT

    -- Timestamps
    login_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_active_at TIMESTAMP,
    expires_at TIMESTAMP,
    logged_out_at TIMESTAMP,

    -- Metadata
    is_current_session BOOLEAN DEFAULT FALSE,
    logout_reason VARCHAR(500),

    -- Base entity fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255),
    version BIGINT NOT NULL DEFAULT 0,

    -- Foreign Keys
    CONSTRAINT fk_session_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_session_refresh_token FOREIGN KEY (refresh_token_id) REFERENCES refresh_tokens(id) ON DELETE SET NULL
);

-- Indexes for performance
CREATE INDEX idx_session_user ON user_sessions(user_id, is_deleted) WHERE is_deleted = FALSE;
CREATE INDEX idx_session_status ON user_sessions(status, is_deleted) WHERE is_deleted = FALSE;
CREATE INDEX idx_session_device ON user_sessions(device_id, is_deleted) WHERE is_deleted = FALSE;
CREATE INDEX idx_session_token ON user_sessions(refresh_token_id, is_deleted) WHERE is_deleted = FALSE;
CREATE INDEX idx_session_active ON user_sessions(user_id, status, last_active_at) WHERE status = 'ACTIVE' AND is_deleted = FALSE;
CREATE INDEX idx_session_expires ON user_sessions(expires_at, status) WHERE status = 'ACTIVE';
CREATE INDEX idx_session_ip ON user_sessions(ip_address, is_deleted) WHERE is_deleted = FALSE;
CREATE INDEX idx_session_location ON user_sessions(country, city, is_deleted) WHERE is_deleted = FALSE;

-- Comments
COMMENT ON TABLE user_sessions IS 'Tracks user login sessions and devices for multi-device management';
COMMENT ON COLUMN user_sessions.device_id IS 'Unique device identifier (generated from user agent or client-provided)';
COMMENT ON COLUMN user_sessions.device_name IS 'User-friendly device name (e.g., "iPhone 12 Pro")';
COMMENT ON COLUMN user_sessions.device_type IS 'Device category: WEB, MOBILE, TABLET, DESKTOP';
COMMENT ON COLUMN user_sessions.country IS 'Country determined from IP address (like Facebook)';
COMMENT ON COLUMN user_sessions.city IS 'City determined from IP address (like Facebook)';
COMMENT ON COLUMN user_sessions.status IS 'Session status: ACTIVE, EXPIRED, REVOKED, LOGGED_OUT';
COMMENT ON COLUMN user_sessions.is_current_session IS 'True if this is the current active session';
COMMENT ON COLUMN user_sessions.logout_reason IS 'Reason for logout (e.g., "User logged out", "Logged out from other device")';

-- Trigger for updated_at
CREATE OR REPLACE FUNCTION update_user_sessions_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_user_sessions_updated_at
    BEFORE UPDATE ON user_sessions
    FOR EACH ROW
    EXECUTE FUNCTION update_user_sessions_updated_at();

-- Insert completion log
INSERT INTO audit_logs (
    user_identifier,
    user_type,
    http_method,
    endpoint,
    status_code,
    response_time_ms,
    error_message
) VALUES (
    'system',
    'SYSTEM',
    'MIGRATION',
    '/database/migrations/V4__create_user_sessions_table.sql',
    200,
    0,
    'User sessions table created successfully'
);
