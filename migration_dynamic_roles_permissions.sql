-- Migration: Dynamic Roles and Permissions System
-- Description: Adds support for dynamic role creation and permission management
-- Date: 2026-01-24

-- Create permissions table
CREATE TABLE IF NOT EXISTS permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    category VARCHAR(100),
    is_system BOOLEAN NOT NULL DEFAULT false,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_permission_code ON permissions(code);
CREATE INDEX IF NOT EXISTS idx_permission_deleted ON permissions(is_deleted);
CREATE INDEX IF NOT EXISTS idx_permission_category ON permissions(category) WHERE category IS NOT NULL;

-- Update roles table for dynamic role support
ALTER TABLE roles ADD COLUMN IF NOT EXISTS code VARCHAR(100);
ALTER TABLE roles ADD COLUMN IF NOT EXISTS display_name VARCHAR(200);
ALTER TABLE roles ADD COLUMN IF NOT EXISTS scope VARCHAR(50);
ALTER TABLE roles ADD COLUMN IF NOT EXISTS business_id UUID;
ALTER TABLE roles ADD COLUMN IF NOT EXISTS is_system BOOLEAN NOT NULL DEFAULT false;

-- Update existing system roles with codes and scope
UPDATE roles SET code = name::text, display_name = description, scope = 'PLATFORM', is_system = true
WHERE name = 'PLATFORM_OWNER';

UPDATE roles SET code = name::text, display_name = description, scope = 'BUSINESS', is_system = true
WHERE name = 'BUSINESS_OWNER';

UPDATE roles SET code = name::text, display_name = description, scope = 'CUSTOMER', is_system = true
WHERE name = 'CUSTOMER';

-- Make code NOT NULL after populating existing data
ALTER TABLE roles ALTER COLUMN code SET NOT NULL;
ALTER TABLE roles ALTER COLUMN scope SET NOT NULL;

-- Drop old unique constraint on name only
ALTER TABLE roles DROP CONSTRAINT IF EXISTS roles_name_key;

-- Add new indexes
CREATE INDEX IF NOT EXISTS idx_role_code ON roles(code);
CREATE INDEX IF NOT EXISTS idx_role_scope ON roles(scope, is_deleted);
CREATE INDEX IF NOT EXISTS idx_role_business ON roles(business_id, is_deleted);

-- Add unique constraint: code must be unique within business (or globally for platform/customer roles)
CREATE UNIQUE INDEX IF NOT EXISTS uk_role_code_business 
ON roles(code, business_id) WHERE is_deleted = false;

-- Create role_permissions join table
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id)
);

CREATE INDEX IF NOT EXISTS idx_role_permissions_role ON role_permissions(role_id);
CREATE INDEX IF NOT EXISTS idx_role_permissions_permission ON role_permissions(permission_id);

-- Insert default permissions
INSERT INTO permissions (code, name, description, category, is_system) VALUES
('user.view', 'View Users', 'View user information', 'USER_MANAGEMENT', true),
('user.create', 'Create Users', 'Create new users', 'USER_MANAGEMENT', true),
('user.edit', 'Edit Users', 'Edit user information', 'USER_MANAGEMENT', true),
('user.delete', 'Delete Users', 'Delete users', 'USER_MANAGEMENT', true),
('user.reset_password', 'Reset Password', 'Reset user password', 'USER_MANAGEMENT', true),

('role.view', 'View Roles', 'View roles and permissions', 'ROLE_MANAGEMENT', true),
('role.create', 'Create Roles', 'Create custom roles', 'ROLE_MANAGEMENT', true),
('role.edit', 'Edit Roles', 'Edit role permissions', 'ROLE_MANAGEMENT', true),
('role.delete', 'Delete Roles', 'Delete custom roles', 'ROLE_MANAGEMENT', true),

('business.view', 'View Business', 'View business information', 'BUSINESS_MANAGEMENT', true),
('business.edit', 'Edit Business', 'Edit business information', 'BUSINESS_MANAGEMENT', true),
('business.delete', 'Delete Business', 'Delete business', 'BUSINESS_MANAGEMENT', true),

('subscription.view', 'View Subscription', 'View subscription details', 'SUBSCRIPTION_MANAGEMENT', true),
('subscription.manage', 'Manage Subscription', 'Manage subscription plans', 'SUBSCRIPTION_MANAGEMENT', true),

('menu.view', 'View Menu', 'View menu items', 'MENU_MANAGEMENT', true),
('menu.create', 'Create Menu', 'Create menu items', 'MENU_MANAGEMENT', true),
('menu.edit', 'Edit Menu', 'Edit menu items', 'MENU_MANAGEMENT', true),
('menu.delete', 'Delete Menu', 'Delete menu items', 'MENU_MANAGEMENT', true),

('order.view', 'View Orders', 'View order information', 'ORDER_MANAGEMENT', true),
('order.manage', 'Manage Orders', 'Manage order status', 'ORDER_MANAGEMENT', true),

('payment.view', 'View Payments', 'View payment information', 'PAYMENT_MANAGEMENT', true),
('payment.manage', 'Manage Payments', 'Process payments', 'PAYMENT_MANAGEMENT', true),

('report.view', 'View Reports', 'View business reports', 'REPORTING', true),
('report.export', 'Export Reports', 'Export report data', 'REPORTING', true)
ON CONFLICT (code) DO NOTHING;

-- Add comments
COMMENT ON TABLE permissions IS 'Defines available permissions in the system';
COMMENT ON TABLE role_permissions IS 'Maps roles to their permissions';
COMMENT ON COLUMN roles.code IS 'Unique code for the role';
COMMENT ON COLUMN roles.scope IS 'Role scope: PLATFORM, BUSINESS, or CUSTOMER';
COMMENT ON COLUMN roles.business_id IS 'Business ID for business-specific roles';
COMMENT ON COLUMN roles.is_system IS 'System roles cannot be deleted';
