-- =====================================================
-- Migration: Cleanup Duplicate Usernames (If Needed)
-- Purpose: Resolve duplicate usernames before applying unique constraints
-- Date: 2026-01-23
-- =====================================================

-- ⚠️  IMPORTANT: Run this ONLY if V1 migration reported duplicate usernames
-- This script helps you identify and resolve duplicate usernames

-- =====================================================
-- OPTION 1: View duplicate CUSTOMER usernames
-- =====================================================
-- Uncomment to see duplicate customers:
/*
SELECT
    user_identifier,
    COUNT(*) as duplicate_count,
    STRING_AGG(id::TEXT, ', ') as user_ids,
    STRING_AGG(email, ', ') as emails
FROM users
WHERE is_deleted = false AND user_type = 'CUSTOMER'
GROUP BY user_identifier
HAVING COUNT(*) > 1
ORDER BY duplicate_count DESC;
*/

-- =====================================================
-- OPTION 2: View duplicate PLATFORM_USER usernames
-- =====================================================
-- Uncomment to see duplicate platform users:
/*
SELECT
    user_identifier,
    COUNT(*) as duplicate_count,
    STRING_AGG(id::TEXT, ', ') as user_ids,
    STRING_AGG(email, ', ') as emails
FROM users
WHERE is_deleted = false AND user_type = 'PLATFORM_USER'
GROUP BY user_identifier
HAVING COUNT(*) > 1
ORDER BY duplicate_count DESC;
*/

-- =====================================================
-- OPTION 3: View duplicate BUSINESS_USER in same business
-- =====================================================
-- Uncomment to see duplicate business users:
/*
SELECT
    user_identifier,
    business_id,
    COUNT(*) as duplicate_count,
    STRING_AGG(id::TEXT, ', ') as user_ids,
    STRING_AGG(email, ', ') as emails
FROM users
WHERE is_deleted = false AND user_type = 'BUSINESS_USER'
GROUP BY user_identifier, business_id
HAVING COUNT(*) > 1
ORDER BY duplicate_count DESC;
*/

-- =====================================================
-- RESOLUTION STRATEGIES
-- =====================================================

-- Strategy 1: Soft delete duplicates (keep only the oldest user)
-- Uncomment and modify as needed:
/*
WITH ranked_users AS (
    SELECT
        id,
        user_identifier,
        user_type,
        created_at,
        ROW_NUMBER() OVER (PARTITION BY user_identifier, user_type ORDER BY created_at ASC) as rn
    FROM users
    WHERE is_deleted = false AND user_type IN ('CUSTOMER', 'PLATFORM_USER')
)
UPDATE users
SET
    is_deleted = true,
    deleted_at = CURRENT_TIMESTAMP,
    deleted_by = 'MIGRATION_V2_CLEANUP'
WHERE id IN (
    SELECT id FROM ranked_users WHERE rn > 1
);
*/

-- Strategy 2: Rename duplicates by appending a number
-- Uncomment and modify as needed:
/*
WITH ranked_users AS (
    SELECT
        id,
        user_identifier,
        user_type,
        ROW_NUMBER() OVER (PARTITION BY user_identifier, user_type ORDER BY created_at ASC) as rn
    FROM users
    WHERE is_deleted = false AND user_type IN ('CUSTOMER', 'PLATFORM_USER')
)
UPDATE users u
SET user_identifier = u.user_identifier || '_' || r.rn
FROM ranked_users r
WHERE u.id = r.id AND r.rn > 1;
*/

-- Strategy 3: Manual review - Export duplicates to review
-- Run this to export to CSV for manual review:
/*
COPY (
    SELECT
        u.id,
        u.user_identifier,
        u.user_type,
        u.email,
        u.first_name,
        u.last_name,
        u.created_at,
        u.business_id,
        b.name as business_name
    FROM users u
    LEFT JOIN businesses b ON u.business_id = b.id
    WHERE u.is_deleted = false
        AND u.user_identifier IN (
            SELECT user_identifier
            FROM users
            WHERE is_deleted = false AND user_type IN ('CUSTOMER', 'PLATFORM_USER')
            GROUP BY user_identifier, user_type
            HAVING COUNT(*) > 1
        )
    ORDER BY u.user_identifier, u.user_type, u.created_at
) TO '/tmp/duplicate_users.csv' WITH CSV HEADER;
*/

-- =====================================================
-- VERIFICATION QUERY
-- =====================================================
-- Run this after cleanup to verify no duplicates remain:
/*
SELECT 'Customers' as user_type, COUNT(*) as duplicate_count
FROM (
    SELECT user_identifier, COUNT(*) as cnt
    FROM users
    WHERE is_deleted = false AND user_type = 'CUSTOMER'
    GROUP BY user_identifier
    HAVING COUNT(*) > 1
) AS dups

UNION ALL

SELECT 'Platform Users' as user_type, COUNT(*) as duplicate_count
FROM (
    SELECT user_identifier, COUNT(*) as cnt
    FROM users
    WHERE is_deleted = false AND user_type = 'PLATFORM_USER'
    GROUP BY user_identifier
    HAVING COUNT(*) > 1
) AS dups

UNION ALL

SELECT 'Business Users (same business)' as user_type, COUNT(*) as duplicate_count
FROM (
    SELECT user_identifier, business_id, COUNT(*) as cnt
    FROM users
    WHERE is_deleted = false AND user_type = 'BUSINESS_USER' AND business_id IS NOT NULL
    GROUP BY user_identifier, business_id
    HAVING COUNT(*) > 1
) AS dups;
*/

-- ✅ After cleanup, re-run V1 migration to apply unique constraints
