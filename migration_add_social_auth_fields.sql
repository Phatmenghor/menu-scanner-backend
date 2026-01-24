-- Migration: Add Telegram and Google OAuth fields to users table
-- Description: Adds support for Telegram sync and Google OAuth authentication
-- Author: Cambodia E-Menu Platform
-- Date: 2026-01-24

-- Add Telegram fields
ALTER TABLE users ADD COLUMN IF NOT EXISTS telegram_id BIGINT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS telegram_username VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS telegram_first_name VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS telegram_last_name VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS telegram_synced_at TIMESTAMP;

-- Add Google OAuth fields
ALTER TABLE users ADD COLUMN IF NOT EXISTS google_id VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS google_email VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS google_synced_at TIMESTAMP;

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_users_telegram_id ON users(telegram_id) WHERE telegram_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_users_google_id ON users(google_id) WHERE google_id IS NOT NULL;

-- Add unique constraint for telegram_id (one Telegram account per user)
-- Note: This allows NULL values, so multiple users can have NULL telegram_id
CREATE UNIQUE INDEX IF NOT EXISTS uk_users_telegram_id ON users(telegram_id) WHERE telegram_id IS NOT NULL AND is_deleted = false;

-- Add unique constraint for google_id per user type (one Google account per user type)
-- Note: A Google account can be linked to different user types (e.g., same Google for CUSTOMER and BUSINESS_USER)
CREATE UNIQUE INDEX IF NOT EXISTS uk_users_google_id_type ON users(google_id, user_type) WHERE google_id IS NOT NULL AND is_deleted = false;

-- Comments
COMMENT ON COLUMN users.telegram_id IS 'Telegram user ID for bot integration and push notifications';
COMMENT ON COLUMN users.telegram_username IS 'Telegram username (without @)';
COMMENT ON COLUMN users.telegram_first_name IS 'Telegram first name';
COMMENT ON COLUMN users.telegram_last_name IS 'Telegram last name';
COMMENT ON COLUMN users.telegram_synced_at IS 'Timestamp when Telegram was synced';
COMMENT ON COLUMN users.google_id IS 'Google OAuth user ID (sub claim from Google)';
COMMENT ON COLUMN users.google_email IS 'Email from Google OAuth';
COMMENT ON COLUMN users.google_synced_at IS 'Timestamp when Google OAuth was synced';
