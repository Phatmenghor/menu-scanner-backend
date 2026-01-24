-- Migration: Dynamic Roles System
-- Description: Add support for dynamic role creation by PLATFORM_OWNER and BUSINESS_OWNER
-- Date: 2026-01-24

-- Update roles table for dynamic role support
ALTER TABLE roles ADD COLUMN IF NOT EXISTS display_name VARCHAR(200);
ALTER TABLE roles ADD COLUMN IF NOT EXISTS business_id UUID;

-- Change name from enum to varchar
ALTER TABLE roles ALTER COLUMN name TYPE VARCHAR(100);
ALTER TABLE roles ALTER COLUMN name SET NOT NULL;

-- Update existing roles
UPDATE roles SET display_name = description, business_id = NULL
WHERE name IN ('PLATFORM_OWNER', 'BUSINESS_OWNER', 'CUSTOMER');

-- Drop old unique constraint on name only
ALTER TABLE roles DROP CONSTRAINT IF EXISTS roles_name_key;

-- Add new indexes
CREATE INDEX IF NOT EXISTS idx_role_name ON roles(name, is_deleted);
CREATE INDEX IF NOT EXISTS idx_role_business ON roles(business_id, is_deleted);

-- Add unique constraint: name must be unique within business (or globally for platform/customer roles)
CREATE UNIQUE INDEX IF NOT EXISTS uk_role_name_business
ON roles(name, business_id) WHERE is_deleted = false;

-- Comments
COMMENT ON COLUMN roles.name IS 'Role name (e.g., PLATFORM_OWNER, PLATFORM_ADMIN, BUSINESS_STAFF, CUSTOMER)';
COMMENT ON COLUMN roles.business_id IS 'Business ID for business-specific roles (NULL for platform/customer roles)';
COMMENT ON COLUMN roles.display_name IS 'Display name for the role';
COMMENT ON COLUMN roles.description IS 'Description of what this role does';
