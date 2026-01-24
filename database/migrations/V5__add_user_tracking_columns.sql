-- =====================================================
-- Migration V5: Add User Tracking Columns
-- Purpose: Add last login and last active tracking to users table
-- Date: 2026-01-24
-- =====================================================

-- Add tracking columns to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_active_at TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS active_sessions_count INTEGER DEFAULT 0;

-- Create index for active users query
CREATE INDEX IF NOT EXISTS idx_user_last_active ON users(last_active_at, is_deleted) WHERE is_deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_user_last_login ON users(last_login_at, is_deleted) WHERE is_deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_user_active_sessions ON users(active_sessions_count, is_deleted) WHERE is_deleted = FALSE AND active_sessions_count > 0;

-- Comments
COMMENT ON COLUMN users.last_login_at IS 'Timestamp of last successful login';
COMMENT ON COLUMN users.last_active_at IS 'Timestamp of last activity (updated on each request)';
COMMENT ON COLUMN users.active_sessions_count IS 'Number of active login sessions for this user';

-- Initialize last_login_at for existing users from created_at
UPDATE users
SET last_login_at = created_at
WHERE last_login_at IS NULL AND is_deleted = FALSE;

-- Initialize last_active_at for existing users
UPDATE users
SET last_active_at = COALESCE(updated_at, created_at)
WHERE last_active_at IS NULL AND is_deleted = FALSE;

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
    '/database/migrations/V5__add_user_tracking_columns.sql',
    200,
    0,
    'User tracking columns added successfully'
);
