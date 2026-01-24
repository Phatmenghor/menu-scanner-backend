-- =====================================================
-- Migration V3: Create Audit Logs Table
-- Purpose: Create comprehensive audit logging system
-- Date: 2026-01-24
-- =====================================================

-- Drop the table if it exists (to ensure clean migration from Hibernate auto-created table)
-- This is safe because audit logs are not critical business data
DROP TABLE IF EXISTS audit_logs CASCADE;

-- Create audit_logs table with all fields
CREATE TABLE audit_logs (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- User Information (nullable for anonymous access)
    user_id UUID,
    user_identifier VARCHAR(255),
    user_type VARCHAR(50),

    -- Request Information (required)
    http_method VARCHAR(10) NOT NULL,
    endpoint VARCHAR(500) NOT NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),

    -- Response Information
    status_code INTEGER,
    response_time_ms BIGINT,
    error_message VARCHAR(1000),

    -- Session Information
    session_id VARCHAR(100),

    -- Request/Response Details (for detailed logging)
    request_params VARCHAR(2000),
    request_body TEXT,
    response_body TEXT,

    -- Timestamps (inherited from BaseUUIDEntity pattern)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Create indexes for efficient querying
CREATE INDEX idx_audit_user ON audit_logs(user_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_audit_endpoint ON audit_logs(endpoint) WHERE is_deleted = FALSE;
CREATE INDEX idx_audit_ip ON audit_logs(ip_address) WHERE is_deleted = FALSE;
CREATE INDEX idx_audit_created ON audit_logs(created_at) WHERE is_deleted = FALSE;
CREATE INDEX idx_audit_status ON audit_logs(status_code) WHERE is_deleted = FALSE;
CREATE INDEX idx_audit_user_type ON audit_logs(user_type) WHERE is_deleted = FALSE;
CREATE INDEX idx_audit_http_method ON audit_logs(http_method) WHERE is_deleted = FALSE;

-- Composite indexes for common queries
CREATE INDEX idx_audit_user_created ON audit_logs(user_id, created_at DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_audit_endpoint_created ON audit_logs(endpoint, created_at DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_audit_status_created ON audit_logs(status_code, created_at DESC) WHERE is_deleted = FALSE;

-- Index for error logs (status_code >= 400)
CREATE INDEX idx_audit_errors ON audit_logs(status_code, created_at DESC)
    WHERE status_code >= 400 AND is_deleted = FALSE;

-- Index for anonymous access (no user_id)
CREATE INDEX idx_audit_anonymous ON audit_logs(created_at DESC)
    WHERE user_id IS NULL AND is_deleted = FALSE;

-- Add comments for documentation
COMMENT ON TABLE audit_logs IS 'Comprehensive audit log for all API requests and responses';
COMMENT ON COLUMN audit_logs.user_id IS 'User ID from users table (NULL for anonymous requests)';
COMMENT ON COLUMN audit_logs.user_identifier IS 'Username or identifier (anonymous for unauthenticated requests)';
COMMENT ON COLUMN audit_logs.user_type IS 'User type: CUSTOMER, BUSINESS_USER, PLATFORM_USER, or ANONYMOUS';
COMMENT ON COLUMN audit_logs.http_method IS 'HTTP method: GET, POST, PUT, DELETE, etc.';
COMMENT ON COLUMN audit_logs.endpoint IS 'API endpoint path';
COMMENT ON COLUMN audit_logs.ip_address IS 'Client IP address (from X-Forwarded-For or remote address)';
COMMENT ON COLUMN audit_logs.user_agent IS 'Client user agent string';
COMMENT ON COLUMN audit_logs.status_code IS 'HTTP response status code';
COMMENT ON COLUMN audit_logs.response_time_ms IS 'Response time in milliseconds';
COMMENT ON COLUMN audit_logs.error_message IS 'Error message if request failed';
COMMENT ON COLUMN audit_logs.session_id IS 'HTTP session ID if available';
COMMENT ON COLUMN audit_logs.request_params IS 'Query parameters and form data';
COMMENT ON COLUMN audit_logs.request_body IS 'Request body for POST/PUT/PATCH/DELETE requests';
COMMENT ON COLUMN audit_logs.response_body IS 'Response body for POST/PUT/PATCH/DELETE requests';

-- Create a function to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_audit_logs_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to auto-update updated_at
CREATE TRIGGER trigger_audit_logs_updated_at
    BEFORE UPDATE ON audit_logs
    FOR EACH ROW
    EXECUTE FUNCTION update_audit_logs_updated_at();

-- Insert a migration audit log entry
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
    '/database/migrations/V3__create_audit_logs_table.sql',
    200,
    0,
    'Audit logs table created successfully'
);

-- Print completion message
DO $$
BEGIN
    RAISE NOTICE 'Migration V3 completed: Audit logs table created with % indexes',
        (SELECT COUNT(*) FROM pg_indexes WHERE tablename = 'audit_logs');
END $$;
