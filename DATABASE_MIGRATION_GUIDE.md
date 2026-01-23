# Database Migration Guide - Dynamic Username Uniqueness

## Overview
This guide explains how to migrate your existing PostgreSQL database to support the new dynamic username uniqueness feature.

## What Changed?

### Before Migration
- Usernames were globally unique across ALL user types
- Example: Only ONE "john" could exist in the entire system

### After Migration
The SAME username can now exist in different contexts:

| User Type | Uniqueness Rule | Example |
|-----------|----------------|---------|
| **CUSTOMER** | Globally unique among customers | Only ONE customer "john" |
| **PLATFORM_USER** | Globally unique among platform users | Only ONE platform user "john" |
| **BUSINESS_USER** | Unique per business | Multiple "john" users, one per business |

**Real-World Example:**
```
✅ Username "john" can exist as:
   - Customer "john" (customer-id-1)
   - Platform user "john" (platform-id-1)
   - Business user "john" in Restaurant A (business-user-id-1)
   - Business user "john" in Restaurant B (business-user-id-2)
   - Business user "john" in Restaurant C (business-user-id-3)

❌ But CANNOT exist as:
   - Two customers both named "john"
   - Two platform users both named "john"
   - Two business users both named "john" in the SAME restaurant
```

## Migration Steps

### Step 1: Backup Your Database
```bash
# Create a backup before migration
pg_dump -U postgres -d e_menu_platform > backup_before_migration_$(date +%Y%m%d_%H%M%S).sql
```

### Step 2: Run Data Integrity Check
```bash
# Connect to PostgreSQL
psql -U postgres -d e_menu_platform

# Run the migration script (it will check for duplicates)
\i database/migrations/V1__dynamic_username_uniqueness.sql
```

### Step 3: Handle Duplicates (If Any)

#### Option A: View Duplicates
```sql
-- View duplicate CUSTOMER usernames
SELECT
    user_identifier,
    COUNT(*) as duplicate_count,
    STRING_AGG(email, ', ') as emails
FROM users
WHERE is_deleted = false AND user_type = 'CUSTOMER'
GROUP BY user_identifier
HAVING COUNT(*) > 1;

-- View duplicate PLATFORM_USER usernames
SELECT
    user_identifier,
    COUNT(*) as duplicate_count,
    STRING_AGG(email, ', ') as emails
FROM users
WHERE is_deleted = false AND user_type = 'PLATFORM_USER'
GROUP BY user_identifier
HAVING COUNT(*) > 1;

-- View duplicate BUSINESS_USER in same business
SELECT
    user_identifier,
    business_id,
    COUNT(*) as duplicate_count
FROM users
WHERE is_deleted = false AND user_type = 'BUSINESS_USER'
GROUP BY user_identifier, business_id
HAVING COUNT(*) > 1;
```

#### Option B: Auto-Resolve Duplicates
```sql
-- Keep oldest user, soft-delete newer duplicates
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
    deleted_by = 'MIGRATION_CLEANUP'
WHERE id IN (
    SELECT id FROM ranked_users WHERE rn > 1
);
```

#### Option C: Rename Duplicates
```sql
-- Append number to duplicate usernames
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
```

### Step 4: Verify Migration Success
```sql
-- Check that unique constraints were created
SELECT
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename = 'users'
  AND indexname LIKE 'uk_%';

-- Expected output:
-- uk_platform_user_identifier  (for CUSTOMER and PLATFORM_USER)
-- uk_business_user_identifier  (for BUSINESS_USER per business)

-- Check that refresh_tokens table was created
SELECT table_name
FROM information_schema.tables
WHERE table_name = 'refresh_tokens';

-- Verify no duplicates remain
SELECT
    'Customers' as user_type,
    COUNT(*) as duplicate_groups
FROM (
    SELECT user_identifier
    FROM users
    WHERE is_deleted = false AND user_type = 'CUSTOMER'
    GROUP BY user_identifier
    HAVING COUNT(*) > 1
) AS dups;
-- Expected: 0 duplicate groups
```

### Step 5: Test the New System
```sql
-- Test 1: Create same username as different user types (should succeed)
BEGIN;

-- Insert "testuser" as CUSTOMER
INSERT INTO users (id, user_identifier, user_type, password, account_status, is_deleted)
VALUES (gen_random_uuid(), 'testuser', 'CUSTOMER', 'hashed_password', 'ACTIVE', false);

-- Insert "testuser" as PLATFORM_USER (should succeed - different type)
INSERT INTO users (id, user_identifier, user_type, password, account_status, is_deleted)
VALUES (gen_random_uuid(), 'testuser', 'PLATFORM_USER', 'hashed_password', 'ACTIVE', false);

-- Insert "testuser" as BUSINESS_USER in business 1 (should succeed)
INSERT INTO users (id, user_identifier, user_type, business_id, password, account_status, is_deleted)
VALUES (gen_random_uuid(), 'testuser', 'BUSINESS_USER', (SELECT id FROM businesses LIMIT 1), 'hashed_password', 'ACTIVE', false);

ROLLBACK; -- Or COMMIT if you want to keep test data
```

```sql
-- Test 2: Try to create duplicate in same context (should fail)
BEGIN;

-- Insert "testuser2" as CUSTOMER
INSERT INTO users (id, user_identifier, user_type, password, account_status, is_deleted)
VALUES (gen_random_uuid(), 'testuser2', 'CUSTOMER', 'hashed_password', 'ACTIVE', false);

-- Try to insert another "testuser2" as CUSTOMER (should fail)
INSERT INTO users (id, user_identifier, user_type, password, account_status, is_deleted)
VALUES (gen_random_uuid(), 'testuser2', 'CUSTOMER', 'hashed_password', 'ACTIVE', false);
-- Expected error: duplicate key value violates unique constraint

ROLLBACK;
```

## Application Changes Required

### Frontend Login Updates
Update your login API calls to include `userType`:

```javascript
// Before
const loginRequest = {
  userIdentifier: "john",
  password: "password123"
};

// After - Customer login
const loginRequest = {
  userIdentifier: "john",
  password: "password123",
  userType: "CUSTOMER"  // REQUIRED
};

// After - Business user login
const loginRequest = {
  userIdentifier: "john",
  password: "password123",
  userType: "BUSINESS_USER",  // REQUIRED
  businessId: "uuid-of-restaurant"  // REQUIRED for business users
};

// After - Platform user login
const loginRequest = {
  userIdentifier: "admin",
  password: "password123",
  userType: "PLATFORM_USER"  // REQUIRED
};
```

## Rollback Plan

If you need to rollback the migration:

```sql
-- Step 1: Drop new constraints and indexes
DROP INDEX IF EXISTS uk_platform_user_identifier;
DROP INDEX IF EXISTS uk_business_user_identifier;
DROP INDEX IF EXISTS idx_user_identifier_type;
DROP INDEX IF EXISTS idx_user_identifier_business;

-- Step 2: Restore old unique constraint
ALTER TABLE users
ADD CONSTRAINT users_user_identifier_key UNIQUE (user_identifier);

-- Step 3: Drop refresh_tokens table (if not needed)
DROP TABLE IF EXISTS refresh_tokens;

-- Step 4: Restore from backup
-- psql -U postgres -d e_menu_platform < backup_before_migration_YYYYMMDD_HHMMSS.sql
```

## Troubleshooting

### Issue: Migration fails with "duplicate key" error
**Solution:** You have duplicate usernames. Run Step 3 to resolve duplicates before applying constraints.

### Issue: "constraint does not exist" when dropping old constraint
**Solution:** The constraint might have a different name. Check with:
```sql
SELECT conname
FROM pg_constraint
WHERE conrelid = 'users'::regclass AND contype = 'u';
```

### Issue: Existing users can't login after migration
**Solution:** Make sure frontend is updated to send `userType` in login requests.

## Post-Migration Verification Checklist

- [ ] Database backup created
- [ ] V1 migration script executed successfully
- [ ] No duplicate usernames remain (verified with queries)
- [ ] Unique constraints created successfully
- [ ] refresh_tokens table created
- [ ] Test login with userType works correctly
- [ ] Frontend updated to send userType
- [ ] Can create same username in different contexts
- [ ] Cannot create duplicate username in same context

## Support

If you encounter issues during migration:
1. Check PostgreSQL logs: `/var/log/postgresql/postgresql-*.log`
2. Review migration script output for error messages
3. Verify PostgreSQL version (requires 12+)
4. Ensure user has proper permissions (CREATE INDEX, ALTER TABLE)

## Summary

This migration enables flexible username management while maintaining data integrity:
- **Customers** can have their preferred usernames
- **Business users** can reuse common names across different restaurants
- **Platform users** maintain unique administrative accounts
- All while preventing duplicates within each context

Migration typically completes in under 1 minute for databases with < 1 million users.
