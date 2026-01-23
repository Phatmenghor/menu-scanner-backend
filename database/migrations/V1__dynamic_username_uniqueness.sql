-- =====================================================
-- Migration: Dynamic Username Uniqueness Implementation
-- Purpose: Allow same username across different user types and businesses
-- Date: 2026-01-23
-- =====================================================

-- STEP 1: Drop existing unique constraint on user_identifier
-- This allows the same username to exist in different contexts
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_user_identifier_key;

-- STEP 2: Add composite indexes for better query performance
-- Index for looking up users by identifier and type (CUSTOMER, PLATFORM_USER)
CREATE INDEX IF NOT EXISTS idx_user_identifier_type
    ON users(user_identifier, user_type, is_deleted);

-- Index for looking up business users by identifier and business
CREATE INDEX IF NOT EXISTS idx_user_identifier_business
    ON users(user_identifier, business_id, is_deleted);

-- STEP 3: Add unique constraints for enforcing uniqueness rules
-- Rule 1: CUSTOMER usernames are globally unique among customers
-- Rule 2: PLATFORM_USER usernames are globally unique among platform users
-- Note: This constraint allows the same username to exist as both CUSTOMER and PLATFORM_USER
CREATE UNIQUE INDEX IF NOT EXISTS uk_platform_user_identifier
    ON users(user_identifier, user_type)
    WHERE is_deleted = false AND (user_type = 'CUSTOMER' OR user_type = 'PLATFORM_USER');

-- Rule 3: BUSINESS_USER usernames are unique per business
-- This allows "john" in Business A, Business B, etc.
CREATE UNIQUE INDEX IF NOT EXISTS uk_business_user_identifier
    ON users(user_identifier, business_id)
    WHERE is_deleted = false AND user_type = 'BUSINESS_USER' AND business_id IS NOT NULL;

-- STEP 4: Data integrity check - find potential duplicate violations
-- Run this query to identify users that would violate the new constraints
DO $$
DECLARE
    customer_duplicates INTEGER;
    platform_duplicates INTEGER;
    business_duplicates INTEGER;
BEGIN
    -- Check for duplicate customers
    SELECT COUNT(*) INTO customer_duplicates
    FROM (
        SELECT user_identifier, COUNT(*) as cnt
        FROM users
        WHERE is_deleted = false AND user_type = 'CUSTOMER'
        GROUP BY user_identifier
        HAVING COUNT(*) > 1
    ) AS dups;

    -- Check for duplicate platform users
    SELECT COUNT(*) INTO platform_duplicates
    FROM (
        SELECT user_identifier, COUNT(*) as cnt
        FROM users
        WHERE is_deleted = false AND user_type = 'PLATFORM_USER'
        GROUP BY user_identifier
        HAVING COUNT(*) > 1
    ) AS dups;

    -- Check for duplicate business users in same business
    SELECT COUNT(*) INTO business_duplicates
    FROM (
        SELECT user_identifier, business_id, COUNT(*) as cnt
        FROM users
        WHERE is_deleted = false AND user_type = 'BUSINESS_USER' AND business_id IS NOT NULL
        GROUP BY user_identifier, business_id
        HAVING COUNT(*) > 1
    ) AS dups;

    -- Raise notice if duplicates found
    IF customer_duplicates > 0 THEN
        RAISE NOTICE '⚠️  Found % duplicate CUSTOMER usernames that need resolution', customer_duplicates;
    END IF;

    IF platform_duplicates > 0 THEN
        RAISE NOTICE '⚠️  Found % duplicate PLATFORM_USER usernames that need resolution', platform_duplicates;
    END IF;

    IF business_duplicates > 0 THEN
        RAISE NOTICE '⚠️  Found % duplicate BUSINESS_USER usernames in same business that need resolution', business_duplicates;
    END IF;

    IF customer_duplicates = 0 AND platform_duplicates = 0 AND business_duplicates = 0 THEN
        RAISE NOTICE '✅ No duplicate usernames found - migration safe to proceed';
    END IF;
END $$;

-- STEP 5: Create refresh_tokens table for refresh token mechanism
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token TEXT NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at TIMESTAMP,
    revocation_reason VARCHAR(255),
    device_info VARCHAR(500),
    ip_address VARCHAR(100),

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255),
    version BIGINT NOT NULL DEFAULT 0,

    -- Foreign key
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for refresh_tokens table
CREATE INDEX IF NOT EXISTS idx_refresh_token ON refresh_tokens(token);
CREATE INDEX IF NOT EXISTS idx_refresh_token_user ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_token_expiry ON refresh_tokens(expiry_date);
CREATE INDEX IF NOT EXISTS idx_refresh_token_revoked ON refresh_tokens(is_revoked);

-- =====================================================
-- Migration Complete
-- =====================================================

-- To verify the migration worked:
-- 1. Check that uk_platform_user_identifier constraint exists:
--    SELECT indexname FROM pg_indexes WHERE tablename = 'users' AND indexname LIKE 'uk_%';
--
-- 2. Check that refresh_tokens table exists:
--    SELECT table_name FROM information_schema.tables WHERE table_name = 'refresh_tokens';
--
-- 3. Verify you can create same username in different contexts:
--    Example data that should work:
--    INSERT INTO users (user_identifier, user_type, ...) VALUES ('john', 'CUSTOMER', ...);
--    INSERT INTO users (user_identifier, user_type, ...) VALUES ('john', 'PLATFORM_USER', ...);
--    INSERT INTO users (user_identifier, user_type, business_id, ...) VALUES ('john', 'BUSINESS_USER', 'business-1-uuid', ...);
--    INSERT INTO users (user_identifier, user_type, business_id, ...) VALUES ('john', 'BUSINESS_USER', 'business-2-uuid', ...);
