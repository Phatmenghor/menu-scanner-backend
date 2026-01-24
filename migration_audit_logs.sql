-- Migration: Audit Logs Table
-- Description: Create audit logs table for monitoring all backend access
-- Date: 2026-01-24

CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID,
    user_identifier VARCHAR(255),
    user_type VARCHAR(50),
    http_method VARCHAR(10) NOT NULL,
    endpoint VARCHAR(500) NOT NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    status_code INTEGER,
    response_time_ms BIGINT,
    error_message VARCHAR(1000),
    session_id VARCHAR(100),
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_audit_user ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_endpoint ON audit_logs(endpoint);
CREATE INDEX IF NOT EXISTS idx_audit_ip ON audit_logs(ip_address);
CREATE INDEX IF NOT EXISTS idx_audit_created ON audit_logs(created_at);
CREATE INDEX IF NOT EXISTS idx_audit_status ON audit_logs(status_code);

COMMENT ON TABLE audit_logs IS 'Audit log for monitoring all backend access';
COMMENT ON COLUMN audit_logs.user_id IS 'User ID if authenticated';
COMMENT ON COLUMN audit_logs.user_identifier IS 'User identifier if authenticated';
COMMENT ON COLUMN audit_logs.endpoint IS 'Request URI';
COMMENT ON COLUMN audit_logs.response_time_ms IS 'Response time in milliseconds';
