-- Migration: Dynamic Roles System
-- Description: Add support for dynamic role creation by PLATFORM_OWNER and BUSINESS_OWNER
-- Date: 2026-01-24

-- Update roles table for dynamic role support
ALTER TABLE roles ADD COLUMN IF NOT EXISTS code VARCHAR(100);
ALTER TABLE roles ADD COLUMN IF NOT EXISTS display_name VARCHAR(200);
ALTER TABLE roles ADD COLUMN IF NOT EXISTS business_id UUID;
ALTER TABLE roles ADD COLUMN IF NOT EXISTS is_system BOOLEAN NOT NULL DEFAULT false;

-- Update existing system roles with codes
UPDATE roles SET code = name::text, display_name = description, is_system = true, business_id = NULL
WHERE name IN ('PLATFORM_OWNER', 'BUSINESS_OWNER', 'CUSTOMER');

-- Make code NOT NULL after populating existing data
ALTER TABLE roles ALTER COLUMN code SET NOT NULL;

-- Make name nullable (for dynamic roles that don't have a RoleEnum value)
ALTER TABLE roles ALTER COLUMN name DROP NOT NULL;

-- Drop old unique constraint on name only
ALTER TABLE roles DROP CONSTRAINT IF EXISTS roles_name_key;

-- Add new indexes
CREATE INDEX IF NOT EXISTS idx_role_code ON roles(code);
CREATE INDEX IF NOT EXISTS idx_role_business ON roles(business_id, is_deleted);

-- Add unique constraint: code must be unique within business (or globally for platform/customer roles)
CREATE UNIQUE INDEX IF NOT EXISTS uk_role_code_business
ON roles(code, business_id) WHERE is_deleted = false;

-- Comments
COMMENT ON COLUMN roles.name IS 'RoleEnum value for 3 system roles (PLATFORM_OWNER, BUSINESS_OWNER, CUSTOMER), NULL for dynamic roles';
COMMENT ON COLUMN roles.code IS 'Unique code for the role (unique per business for BUSINESS roles, globally unique for others)';
COMMENT ON COLUMN roles.business_id IS 'Business ID for business-specific roles (NULL for platform/customer roles)';
COMMENT ON COLUMN roles.is_system IS 'System roles (PLATFORM_OWNER, BUSINESS_OWNER, CUSTOMER) cannot be deleted';
COMMENT ON COLUMN roles.display_name IS 'Display name for the role';
COMMENT ON COLUMN roles.description IS 'Description of what this role does';
