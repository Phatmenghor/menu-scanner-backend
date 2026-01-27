-- =====================================================================
-- E-Menu Platform: Customer Role Data Generation Script
-- Platform: CUSTOMER with all roles per business
-- Run this in pgAdmin to populate customer and business role data
-- =====================================================================

-- =====================================================================
-- 1. INSERT BUSINESSES
-- =====================================================================
INSERT INTO businesses (id, name, email, phone, address, description, status, is_subscription_active, owner_id, version, is_deleted, created_at, updated_at, created_by, updated_by)
VALUES
    ('a1000000-0000-0000-0000-000000000001'::uuid, 'Sokha Restaurant', 'sokha@emenu.com', '+855-12-000-001', 'No. 12, Street 240, Phnom Penh', 'Traditional Khmer cuisine restaurant in Phnom Penh', 'ACTIVE', true, NULL, 0, false, NOW(), NOW(), 'system', 'system'),
    ('a1000000-0000-0000-0000-000000000002'::uuid, 'Malis Fine Dining', 'malis@emenu.com', '+855-12-000-002', 'No. 136, Norodom Blvd, Phnom Penh', 'Upscale Cambodian fine dining experience', 'ACTIVE', true, NULL, 0, false, NOW(), NOW(), 'system', 'system'),
    ('a1000000-0000-0000-0000-000000000003'::uuid, 'Bayon Cafe', 'bayon@emenu.com', '+855-12-000-003', 'Pub Street, Siem Reap', 'Popular cafe near Angkor Wat temples', 'ACTIVE', true, NULL, 0, false, NOW(), NOW(), 'system', 'system'),
    ('a1000000-0000-0000-0000-000000000004'::uuid, 'Kampot Pepper House', 'kampot@emenu.com', '+855-12-000-004', 'Riverside Road, Kampot', 'Farm-to-table restaurant specializing in Kampot pepper dishes', 'ACTIVE', true, NULL, 0, false, NOW(), NOW(), 'system', 'system'),
    ('a1000000-0000-0000-0000-000000000005'::uuid, 'Sihanoukville Beach Bar', 'beach@emenu.com', '+855-12-000-005', 'Otres Beach, Sihanoukville', 'Beachfront bar and grill with fresh seafood', 'ACTIVE', false, NULL, 0, false, NOW(), NOW(), 'system', 'system')
ON CONFLICT (id) DO NOTHING;

-- =====================================================================
-- 2. INSERT BUSINESS OWNER USERS (one per business)
-- =====================================================================
INSERT INTO users (id, user_identifier, email, password, first_name, last_name, phone_number, user_type, account_status, business_id, position, version, is_deleted, created_at, updated_at, created_by, updated_by)
VALUES
    ('b1000000-0000-0000-0000-000000000001'::uuid, 'sokha-owner', 'sokha-owner@emenu.com', '$2a$10$dummyHashedPassword000000000000000000000000000001', 'Sokha', 'Chea', '+855-12-100-001', 'BUSINESS_USER', 'ACTIVE', 'a1000000-0000-0000-0000-000000000001'::uuid, 'Owner', 0, false, NOW(), NOW(), 'system', 'system'),
    ('b1000000-0000-0000-0000-000000000002'::uuid, 'malis-owner', 'malis-owner@emenu.com', '$2a$10$dummyHashedPassword000000000000000000000000000002', 'Malis', 'Sok', '+855-12-100-002', 'BUSINESS_USER', 'ACTIVE', 'a1000000-0000-0000-0000-000000000002'::uuid, 'Owner', 0, false, NOW(), NOW(), 'system', 'system'),
    ('b1000000-0000-0000-0000-000000000003'::uuid, 'bayon-owner', 'bayon-owner@emenu.com', '$2a$10$dummyHashedPassword000000000000000000000000000003', 'Bayon', 'Phan', '+855-12-100-003', 'BUSINESS_USER', 'ACTIVE', 'a1000000-0000-0000-0000-000000000003'::uuid, 'Owner', 0, false, NOW(), NOW(), 'system', 'system'),
    ('b1000000-0000-0000-0000-000000000004'::uuid, 'kampot-owner', 'kampot-owner@emenu.com', '$2a$10$dummyHashedPassword000000000000000000000000000004', 'Kampot', 'Ny', '+855-12-100-004', 'BUSINESS_USER', 'ACTIVE', 'a1000000-0000-0000-0000-000000000004'::uuid, 'Owner', 0, false, NOW(), NOW(), 'system', 'system'),
    ('b1000000-0000-0000-0000-000000000005'::uuid, 'beach-owner', 'beach-owner@emenu.com', '$2a$10$dummyHashedPassword000000000000000000000000000005', 'Visal', 'Kim', '+855-12-100-005', 'BUSINESS_USER', 'ACTIVE', 'a1000000-0000-0000-0000-000000000005'::uuid, 'Owner', 0, false, NOW(), NOW(), 'system', 'system')
ON CONFLICT (id) DO NOTHING;

-- Update businesses with owner IDs
UPDATE businesses SET owner_id = 'b1000000-0000-0000-0000-000000000001'::uuid WHERE id = 'a1000000-0000-0000-0000-000000000001'::uuid;
UPDATE businesses SET owner_id = 'b1000000-0000-0000-0000-000000000002'::uuid WHERE id = 'a1000000-0000-0000-0000-000000000002'::uuid;
UPDATE businesses SET owner_id = 'b1000000-0000-0000-0000-000000000003'::uuid WHERE id = 'a1000000-0000-0000-0000-000000000003'::uuid;
UPDATE businesses SET owner_id = 'b1000000-0000-0000-0000-000000000004'::uuid WHERE id = 'a1000000-0000-0000-0000-000000000004'::uuid;
UPDATE businesses SET owner_id = 'b1000000-0000-0000-0000-000000000005'::uuid WHERE id = 'a1000000-0000-0000-0000-000000000005'::uuid;

-- =====================================================================
-- 3. INSERT ROLES PER BUSINESS (all role types for each business)
--    Each business gets: MANAGER, CASHIER, WAITER, CHEF, DELIVERY
--    Plus the global system roles already exist (PLATFORM_OWNER, BUSINESS_OWNER, CUSTOMER)
-- =====================================================================

-- ===================== Sokha Restaurant Roles ========================
INSERT INTO roles (id, name, display_name, description, business_id, user_type, version, is_deleted, created_at, updated_at, created_by, updated_by)
VALUES
    ('c1000000-0001-0000-0000-000000000001'::uuid, 'MANAGER', 'Manager', 'Restaurant manager with full operational access', 'a1000000-0000-0000-0000-000000000001'::uuid, 'BUSINESS_USER', 0, false, NOW(), NOW(), 'system', 'system'),
    ('c1000000-0001-0000-0000-000000000002'::uuid, 'CASHIER', 'Cashier', 'Handles payments and billing', 'a1000000-0000-0000-0000-000000000001'::uuid, 'BUSINESS_USER', 0, false, NOW(), NOW(), 'system', 'system'),
    ('c1000000-0001-0000-0000-000000000003'::uuid, 'WAITER', 'Waiter', 'Takes orders and serves customers', 'a1000000-0000-0000-0000-000000000001'::uuid, 'BUSINESS_USER', 0, false, NOW(), NOW(), 'system', 'system'),
    ('c1000000-0001-0000-0000-000000000004'::uuid, 'CHEF', 'Chef', 'Kitchen staff managing food preparation', 'a1000000-0000-0000-0000-000000000001'::uuid, 'BUSINESS_USER', 0, false, NOW(), NOW(), 'system', 'system'),
    ('c1000000-0001-0000-0000-000000000005'::uuid, 'DELIVERY', 'Delivery', 'Handles food delivery to customers', 'a1000000-0000-0000-0000-000000000001'::uuid, 'BUSINESS_USER', 0, false, NOW(), NOW(), 'system', 'system')
ON CONFLICT ON CONSTRAINT uk_role_name_business DO NOTHING;

-- ===================== Malis Fine Dining Roles =======================
INSERT INTO roles (id, name, display_name, description, business_id, user_type, version, is_deleted, created_at, updated_at, created_by, updated_by)
VALUES
    ('c1000000-0002-0000-0000-000000000001'::uuid, 'MANAGER', 'Manager', 'Restaurant manager with full operational access', 'a1000000-0000-0000-0000-000000000002'::uuid, 'BUSINESS_USER', 0, false, NOW(), NOW(), 'system', 'system'),
    ('c1000000-0002-0000-0000-000000000002'::uuid, 'CASHIER', 'Cashier', 'Handles payments and billing', 'a1000000-0000-0000-0000-000000000002'::uuid, 'BUSINESS_USER', 0, false, NOW(), NOW(), 'system', 'system'),
    ('c1000000-0002-0000-0000-000000000003'::uuid, 'WAITER', 'Waiter', 'Takes orders and serves customers', 'a1000000-0000-0000-0000-000000000002'::uuid, 'BUSINESS_USER', 0, false, NOW(), NOW(), 'system', 'system'),
    ('c1000000-0002-0000-0000-000000000004'::uuid, 'CHEF', 'Chef', 'Kitchen staff managing food preparation', 'a1000000-0000-0000-0000-000000000002'::uuid, 'BUSINESS_USER', 0, false, NOW(), NOW(), 'system', 'system'),
    ('c1000000-0002-0000-0000-000000000005'::uuid, 'DELIVERY', 'Delivery', 'Handles food delivery to customers', 'a1000000-0000-0000-0000-000000000002'::uuid, 'BUSINESS_USER', 0, false, NOW(), NOW(), 'system', 'system')
ON CONFLICT ON CONSTRAINT uk_role_name_business DO NOTHING;

-- ===================== Bayon Cafe Roles ==============================
INSERT INTO roles (id, name, display_name, description, business_id, user_type, version, is_deleted, created_at, updated_at, created_by, updated_by)
VALUES
    ('c1000000-0003-0000-0000-000000000001'::uuid, 'MANAGER', 'Manager', 'Cafe manager with full operational access', 'a1000000-0000-0000-0000-000000000003'::uuid, 'BUSINESS_USER', 0, false, NOW(), NOW(), 'system', 'system'),
    ('c1000000-0003-0000-0000-000000000002'::uuid, 'CASHIER', 'Cashier', 'Handles payments and billing', 'a1000000-0000-0000-0000-000000000003'::uuid, 'BUSINESS_USER', 0, false, NOW(), NOW(), 'system', 'system'),
    ('c1000000-0003-0000-0000-000000000003'::uuid, 'WAITER', 'Waiter', 'Takes orders and serves customers', 'a1000000-0000-0000-0000-000000000003'::uuid, 'BUSINESS_USER', 0, false, NOW(), NOW(), 'system', 'system'),
    ('c1000000-0003-0000-0000-000000000004'::uuid, 'CHEF', 'Chef', 'Kitchen staff managing food preparation', 'a1000000-0000-0000-0000-000000000003'::uuid, 'BUSINESS_USER', 0, false, NOW(), NOW(), 'system', 'system'),
    ('c1000000-0003-0000-0000-000000000005'::uuid, 'DELIVERY', 'Delivery', 'Handles food delivery to customers', 'a1000000-0000-0000-0000-000000000003'::uuid, 'BUSINESS_USER', 0, false, NOW(), NOW(), 'system', 'system')
ON CONFLICT ON CONSTRAINT uk_role_name_business DO NOTHING;

-- ===================== Kampot Pepper House Roles =====================
INSERT INTO roles (id, name, display_name, description, business_id, user_type, version, is_deleted, created_at, updated_at, created_by, updated_by)
VALUES
    ('c1000000-0004-0000-0000-000000000001'::uuid, 'MANAGER', 'Manager', 'Restaurant manager with full operational access', 'a1000000-0000-0000-0000-000000000004'::uuid, 'BUSINESS_USER', 0, false, NOW(), NOW(), 'system', 'system'),
    ('c1000000-0004-0000-0000-000000000002'::uuid, 'CASHIER', 'Cashier', 'Handles payments and billing', 'a1000000-0000-0000-0000-000000000004'::uuid, 'BUSINESS_USER', 0, false, NOW(), NOW(), 'system', 'system'),
    ('c1000000-0004-0000-0000-000000000003'::uuid, 'WAITER', 'Waiter', 'Takes orders and serves customers', 'a1000000-0000-0000-0000-000000000004'::uuid, 'BUSINESS_USER', 0, false, NOW(), NOW(), 'system', 'system'),
    ('c1000000-0004-0000-0000-000000000004'::uuid, 'CHEF', 'Chef', 'Kitchen staff managing food preparation', 'a1000000-0000-0000-0000-000000000004'::uuid, 'BUSINESS_USER', 0, false, NOW(), NOW(), 'system', 'system'),
    ('c1000000-0004-0000-0000-000000000005'::uuid, 'DELIVERY', 'Delivery', 'Handles food delivery to customers', 'a1000000-0000-0000-0000-000000000004'::uuid, 'BUSINESS_USER', 0, false, NOW(), NOW(), 'system', 'system')
ON CONFLICT ON CONSTRAINT uk_role_name_business DO NOTHING;

-- ===================== Sihanoukville Beach Bar Roles =================
INSERT INTO roles (id, name, display_name, description, business_id, user_type, version, is_deleted, created_at, updated_at, created_by, updated_by)
VALUES
    ('c1000000-0005-0000-0000-000000000001'::uuid, 'MANAGER', 'Manager', 'Bar manager with full operational access', 'a1000000-0000-0000-0000-000000000005'::uuid, 'BUSINESS_USER', 0, false, NOW(), NOW(), 'system', 'system'),
    ('c1000000-0005-0000-0000-000000000002'::uuid, 'CASHIER', 'Cashier', 'Handles payments and billing', 'a1000000-0000-0000-0000-000000000005'::uuid, 'BUSINESS_USER', 0, false, NOW(), NOW(), 'system', 'system'),
    ('c1000000-0005-0000-0000-000000000003'::uuid, 'WAITER', 'Waiter', 'Takes orders and serves customers', 'a1000000-0000-0000-0000-000000000005'::uuid, 'BUSINESS_USER', 0, false, NOW(), NOW(), 'system', 'system'),
    ('c1000000-0005-0000-0000-000000000004'::uuid, 'CHEF', 'Chef', 'Kitchen staff managing food preparation', 'a1000000-0000-0000-0000-000000000005'::uuid, 'BUSINESS_USER', 0, false, NOW(), NOW(), 'system', 'system'),
    ('c1000000-0005-0000-0000-000000000005'::uuid, 'DELIVERY', 'Delivery', 'Handles food delivery to customers', 'a1000000-0000-0000-0000-000000000005'::uuid, 'BUSINESS_USER', 0, false, NOW(), NOW(), 'system', 'system')
ON CONFLICT ON CONSTRAINT uk_role_name_business DO NOTHING;

-- =====================================================================
-- 4. INSERT CUSTOMER USERS (platform customers who use the menu app)
-- =====================================================================
INSERT INTO users (id, user_identifier, email, password, first_name, last_name, phone_number, user_type, account_status, position, version, is_deleted, created_at, updated_at, created_by, updated_by)
VALUES
    ('d1000000-0000-0000-0000-000000000001'::uuid, 'customer-dara', 'dara@gmail.com', '$2a$10$dummyHashedPassword000000000000000000000000000101', 'Dara', 'Sorn', '+855-12-200-001', 'CUSTOMER', 'ACTIVE', NULL, 0, false, NOW(), NOW(), 'system', 'system'),
    ('d1000000-0000-0000-0000-000000000002'::uuid, 'customer-sophea', 'sophea@gmail.com', '$2a$10$dummyHashedPassword000000000000000000000000000102', 'Sophea', 'Chhim', '+855-12-200-002', 'CUSTOMER', 'ACTIVE', NULL, 0, false, NOW(), NOW(), 'system', 'system'),
    ('d1000000-0000-0000-0000-000000000003'::uuid, 'customer-virak', 'virak@gmail.com', '$2a$10$dummyHashedPassword000000000000000000000000000103', 'Virak', 'Meas', '+855-12-200-003', 'CUSTOMER', 'ACTIVE', NULL, 0, false, NOW(), NOW(), 'system', 'system'),
    ('d1000000-0000-0000-0000-000000000004'::uuid, 'customer-channary', 'channary@gmail.com', '$2a$10$dummyHashedPassword000000000000000000000000000104', 'Channary', 'Keo', '+855-12-200-004', 'CUSTOMER', 'ACTIVE', NULL, 0, false, NOW(), NOW(), 'system', 'system'),
    ('d1000000-0000-0000-0000-000000000005'::uuid, 'customer-bopha', 'bopha@gmail.com', '$2a$10$dummyHashedPassword000000000000000000000000000105', 'Bopha', 'Ith', '+855-12-200-005', 'CUSTOMER', 'ACTIVE', NULL, 0, false, NOW(), NOW(), 'system', 'system'),
    ('d1000000-0000-0000-0000-000000000006'::uuid, 'customer-vicheka', 'vicheka@gmail.com', '$2a$10$dummyHashedPassword000000000000000000000000000106', 'Vicheka', 'Heng', '+855-12-200-006', 'CUSTOMER', 'ACTIVE', NULL, 0, false, NOW(), NOW(), 'system', 'system'),
    ('d1000000-0000-0000-0000-000000000007'::uuid, 'customer-sreymom', 'sreymom@gmail.com', '$2a$10$dummyHashedPassword000000000000000000000000000107', 'Sreymom', 'Phon', '+855-12-200-007', 'CUSTOMER', 'ACTIVE', NULL, 0, false, NOW(), NOW(), 'system', 'system'),
    ('d1000000-0000-0000-0000-000000000008'::uuid, 'customer-ratana', 'ratana@gmail.com', '$2a$10$dummyHashedPassword000000000000000000000000000108', 'Ratana', 'Tep', '+855-12-200-008', 'CUSTOMER', 'ACTIVE', NULL, 0, false, NOW(), NOW(), 'system', 'system'),
    ('d1000000-0000-0000-0000-000000000009'::uuid, 'customer-kosal', 'kosal@gmail.com', '$2a$10$dummyHashedPassword000000000000000000000000000109', 'Kosal', 'Oum', '+855-12-200-009', 'CUSTOMER', 'ACTIVE', NULL, 0, false, NOW(), NOW(), 'system', 'system'),
    ('d1000000-0000-0000-0000-000000000010'::uuid, 'customer-chenda', 'chenda@gmail.com', '$2a$10$dummyHashedPassword000000000000000000000000000110', 'Chenda', 'Pen', '+855-12-200-010', 'CUSTOMER', 'ACTIVE', NULL, 0, false, NOW(), NOW(), 'system', 'system')
ON CONFLICT (id) DO NOTHING;

-- =====================================================================
-- 5. INSERT BUSINESS STAFF USERS (employees per business)
-- =====================================================================

-- Sokha Restaurant Staff
INSERT INTO users (id, user_identifier, email, password, first_name, last_name, phone_number, user_type, account_status, business_id, position, version, is_deleted, created_at, updated_at, created_by, updated_by)
VALUES
    ('e1000000-0001-0000-0000-000000000001'::uuid, 'sokha-manager', 'sokha-manager@emenu.com', '$2a$10$dummyHashedPassword000000000000000000000000000201', 'Chanthy', 'Lim', '+855-12-300-001', 'BUSINESS_USER', 'ACTIVE', 'a1000000-0000-0000-0000-000000000001'::uuid, 'Manager', 0, false, NOW(), NOW(), 'system', 'system'),
    ('e1000000-0001-0000-0000-000000000002'::uuid, 'sokha-cashier', 'sokha-cashier@emenu.com', '$2a$10$dummyHashedPassword000000000000000000000000000202', 'Srey', 'Neth', '+855-12-300-002', 'BUSINESS_USER', 'ACTIVE', 'a1000000-0000-0000-0000-000000000001'::uuid, 'Cashier', 0, false, NOW(), NOW(), 'system', 'system'),
    ('e1000000-0001-0000-0000-000000000003'::uuid, 'sokha-waiter', 'sokha-waiter@emenu.com', '$2a$10$dummyHashedPassword000000000000000000000000000203', 'Piseth', 'Kong', '+855-12-300-003', 'BUSINESS_USER', 'ACTIVE', 'a1000000-0000-0000-0000-000000000001'::uuid, 'Waiter', 0, false, NOW(), NOW(), 'system', 'system'),
    ('e1000000-0001-0000-0000-000000000004'::uuid, 'sokha-chef', 'sokha-chef@emenu.com', '$2a$10$dummyHashedPassword000000000000000000000000000204', 'Narin', 'Yun', '+855-12-300-004', 'BUSINESS_USER', 'ACTIVE', 'a1000000-0000-0000-0000-000000000001'::uuid, 'Chef', 0, false, NOW(), NOW(), 'system', 'system')
ON CONFLICT (id) DO NOTHING;

-- Malis Fine Dining Staff
INSERT INTO users (id, user_identifier, email, password, first_name, last_name, phone_number, user_type, account_status, business_id, position, version, is_deleted, created_at, updated_at, created_by, updated_by)
VALUES
    ('e1000000-0002-0000-0000-000000000001'::uuid, 'malis-manager', 'malis-manager@emenu.com', '$2a$10$dummyHashedPassword000000000000000000000000000211', 'Sokunthea', 'Van', '+855-12-310-001', 'BUSINESS_USER', 'ACTIVE', 'a1000000-0000-0000-0000-000000000002'::uuid, 'Manager', 0, false, NOW(), NOW(), 'system', 'system'),
    ('e1000000-0002-0000-0000-000000000002'::uuid, 'malis-cashier', 'malis-cashier@emenu.com', '$2a$10$dummyHashedPassword000000000000000000000000000212', 'Leakena', 'Ung', '+855-12-310-002', 'BUSINESS_USER', 'ACTIVE', 'a1000000-0000-0000-0000-000000000002'::uuid, 'Cashier', 0, false, NOW(), NOW(), 'system', 'system'),
    ('e1000000-0002-0000-0000-000000000003'::uuid, 'malis-waiter', 'malis-waiter@emenu.com', '$2a$10$dummyHashedPassword000000000000000000000000000213', 'Vuthy', 'Khem', '+855-12-310-003', 'BUSINESS_USER', 'ACTIVE', 'a1000000-0000-0000-0000-000000000002'::uuid, 'Waiter', 0, false, NOW(), NOW(), 'system', 'system'),
    ('e1000000-0002-0000-0000-000000000004'::uuid, 'malis-chef', 'malis-chef@emenu.com', '$2a$10$dummyHashedPassword000000000000000000000000000214', 'Sameth', 'Long', '+855-12-310-004', 'BUSINESS_USER', 'ACTIVE', 'a1000000-0000-0000-0000-000000000002'::uuid, 'Chef', 0, false, NOW(), NOW(), 'system', 'system')
ON CONFLICT (id) DO NOTHING;

-- Bayon Cafe Staff
INSERT INTO users (id, user_identifier, email, password, first_name, last_name, phone_number, user_type, account_status, business_id, position, version, is_deleted, created_at, updated_at, created_by, updated_by)
VALUES
    ('e1000000-0003-0000-0000-000000000001'::uuid, 'bayon-manager', 'bayon-manager@emenu.com', '$2a$10$dummyHashedPassword000000000000000000000000000221', 'Chanthou', 'Sar', '+855-12-320-001', 'BUSINESS_USER', 'ACTIVE', 'a1000000-0000-0000-0000-000000000003'::uuid, 'Manager', 0, false, NOW(), NOW(), 'system', 'system'),
    ('e1000000-0003-0000-0000-000000000002'::uuid, 'bayon-cashier', 'bayon-cashier@emenu.com', '$2a$10$dummyHashedPassword000000000000000000000000000222', 'Kunthea', 'Ros', '+855-12-320-002', 'BUSINESS_USER', 'ACTIVE', 'a1000000-0000-0000-0000-000000000003'::uuid, 'Cashier', 0, false, NOW(), NOW(), 'system', 'system'),
    ('e1000000-0003-0000-0000-000000000003'::uuid, 'bayon-waiter', 'bayon-waiter@emenu.com', '$2a$10$dummyHashedPassword000000000000000000000000000223', 'Vanna', 'Sin', '+855-12-320-003', 'BUSINESS_USER', 'ACTIVE', 'a1000000-0000-0000-0000-000000000003'::uuid, 'Waiter', 0, false, NOW(), NOW(), 'system', 'system')
ON CONFLICT (id) DO NOTHING;

-- Kampot Pepper House Staff
INSERT INTO users (id, user_identifier, email, password, first_name, last_name, phone_number, user_type, account_status, business_id, position, version, is_deleted, created_at, updated_at, created_by, updated_by)
VALUES
    ('e1000000-0004-0000-0000-000000000001'::uuid, 'kampot-manager', 'kampot-manager@emenu.com', '$2a$10$dummyHashedPassword000000000000000000000000000231', 'Rithea', 'Kem', '+855-12-330-001', 'BUSINESS_USER', 'ACTIVE', 'a1000000-0000-0000-0000-000000000004'::uuid, 'Manager', 0, false, NOW(), NOW(), 'system', 'system'),
    ('e1000000-0004-0000-0000-000000000002'::uuid, 'kampot-cashier', 'kampot-cashier@emenu.com', '$2a$10$dummyHashedPassword000000000000000000000000000232', 'Sopheak', 'Chet', '+855-12-330-002', 'BUSINESS_USER', 'ACTIVE', 'a1000000-0000-0000-0000-000000000004'::uuid, 'Cashier', 0, false, NOW(), NOW(), 'system', 'system')
ON CONFLICT (id) DO NOTHING;

-- Sihanoukville Beach Bar Staff
INSERT INTO users (id, user_identifier, email, password, first_name, last_name, phone_number, user_type, account_status, business_id, position, version, is_deleted, created_at, updated_at, created_by, updated_by)
VALUES
    ('e1000000-0005-0000-0000-000000000001'::uuid, 'beach-manager', 'beach-manager@emenu.com', '$2a$10$dummyHashedPassword000000000000000000000000000241', 'Narith', 'Thorn', '+855-12-340-001', 'BUSINESS_USER', 'ACTIVE', 'a1000000-0000-0000-0000-000000000005'::uuid, 'Manager', 0, false, NOW(), NOW(), 'system', 'system'),
    ('e1000000-0005-0000-0000-000000000002'::uuid, 'beach-waiter', 'beach-waiter@emenu.com', '$2a$10$dummyHashedPassword000000000000000000000000000242', 'Makara', 'Cham', '+855-12-340-002', 'BUSINESS_USER', 'ACTIVE', 'a1000000-0000-0000-0000-000000000005'::uuid, 'Waiter', 0, false, NOW(), NOW(), 'system', 'system')
ON CONFLICT (id) DO NOTHING;

-- =====================================================================
-- 6. ASSIGN ROLES TO USERS (user_roles join table)
-- =====================================================================

-- First, get the system CUSTOMER role ID and BUSINESS_OWNER role ID
-- (These are created by DataInitializationService on app start)
-- We reference them by name below using subqueries

-- Assign CUSTOMER role to all customer users
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.id IN (
    'd1000000-0000-0000-0000-000000000001'::uuid,
    'd1000000-0000-0000-0000-000000000002'::uuid,
    'd1000000-0000-0000-0000-000000000003'::uuid,
    'd1000000-0000-0000-0000-000000000004'::uuid,
    'd1000000-0000-0000-0000-000000000005'::uuid,
    'd1000000-0000-0000-0000-000000000006'::uuid,
    'd1000000-0000-0000-0000-000000000007'::uuid,
    'd1000000-0000-0000-0000-000000000008'::uuid,
    'd1000000-0000-0000-0000-000000000009'::uuid,
    'd1000000-0000-0000-0000-000000000010'::uuid
)
AND r.name = 'CUSTOMER' AND r.business_id IS NULL AND r.is_deleted = false
ON CONFLICT DO NOTHING;

-- Assign BUSINESS_OWNER role to business owners
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.id IN (
    'b1000000-0000-0000-0000-000000000001'::uuid,
    'b1000000-0000-0000-0000-000000000002'::uuid,
    'b1000000-0000-0000-0000-000000000003'::uuid,
    'b1000000-0000-0000-0000-000000000004'::uuid,
    'b1000000-0000-0000-0000-000000000005'::uuid
)
AND r.name = 'BUSINESS_OWNER' AND r.business_id IS NULL AND r.is_deleted = false
ON CONFLICT DO NOTHING;

-- Assign MANAGER role to business managers
INSERT INTO user_roles (user_id, role_id) VALUES
    ('e1000000-0001-0000-0000-000000000001'::uuid, 'c1000000-0001-0000-0000-000000000001'::uuid),  -- Sokha manager
    ('e1000000-0002-0000-0000-000000000001'::uuid, 'c1000000-0002-0000-0000-000000000001'::uuid),  -- Malis manager
    ('e1000000-0003-0000-0000-000000000001'::uuid, 'c1000000-0003-0000-0000-000000000001'::uuid),  -- Bayon manager
    ('e1000000-0004-0000-0000-000000000001'::uuid, 'c1000000-0004-0000-0000-000000000001'::uuid),  -- Kampot manager
    ('e1000000-0005-0000-0000-000000000001'::uuid, 'c1000000-0005-0000-0000-000000000001'::uuid)   -- Beach manager
ON CONFLICT DO NOTHING;

-- Assign CASHIER role to cashiers
INSERT INTO user_roles (user_id, role_id) VALUES
    ('e1000000-0001-0000-0000-000000000002'::uuid, 'c1000000-0001-0000-0000-000000000002'::uuid),  -- Sokha cashier
    ('e1000000-0002-0000-0000-000000000002'::uuid, 'c1000000-0002-0000-0000-000000000002'::uuid),  -- Malis cashier
    ('e1000000-0003-0000-0000-000000000002'::uuid, 'c1000000-0003-0000-0000-000000000002'::uuid),  -- Bayon cashier
    ('e1000000-0004-0000-0000-000000000002'::uuid, 'c1000000-0004-0000-0000-000000000002'::uuid)   -- Kampot cashier
ON CONFLICT DO NOTHING;

-- Assign WAITER role to waiters
INSERT INTO user_roles (user_id, role_id) VALUES
    ('e1000000-0001-0000-0000-000000000003'::uuid, 'c1000000-0001-0000-0000-000000000003'::uuid),  -- Sokha waiter
    ('e1000000-0002-0000-0000-000000000003'::uuid, 'c1000000-0002-0000-0000-000000000003'::uuid),  -- Malis waiter
    ('e1000000-0003-0000-0000-000000000003'::uuid, 'c1000000-0003-0000-0000-000000000003'::uuid),  -- Bayon waiter
    ('e1000000-0005-0000-0000-000000000002'::uuid, 'c1000000-0005-0000-0000-000000000003'::uuid)   -- Beach waiter
ON CONFLICT DO NOTHING;

-- Assign CHEF role to chefs
INSERT INTO user_roles (user_id, role_id) VALUES
    ('e1000000-0001-0000-0000-000000000004'::uuid, 'c1000000-0001-0000-0000-000000000004'::uuid),  -- Sokha chef
    ('e1000000-0002-0000-0000-000000000004'::uuid, 'c1000000-0002-0000-0000-000000000004'::uuid),  -- Malis chef
    ('e1000000-0003-0000-0000-000000000003'::uuid, 'c1000000-0003-0000-0000-000000000004'::uuid)   -- Bayon chef (waiter also serves as chef)
ON CONFLICT DO NOTHING;

-- =====================================================================
-- 7. VERIFICATION QUERIES (run these to verify data was inserted)
-- =====================================================================

-- Check all businesses
-- SELECT id, name, email, status, is_subscription_active FROM businesses WHERE is_deleted = false ORDER BY name;

-- Check all roles grouped by business
-- SELECT r.name, r.display_name, r.user_type, b.name as business_name
-- FROM roles r
-- LEFT JOIN businesses b ON r.business_id = b.id
-- WHERE r.is_deleted = false
-- ORDER BY b.name NULLS FIRST, r.name;

-- Check all customer users
-- SELECT id, user_identifier, email, first_name, last_name, user_type, account_status
-- FROM users WHERE user_type = 'CUSTOMER' AND is_deleted = false ORDER BY first_name;

-- Check all user-role assignments
-- SELECT u.user_identifier, u.user_type, r.name as role_name, r.user_type as role_type, b.name as business_name
-- FROM user_roles ur
-- JOIN users u ON ur.user_id = u.id
-- JOIN roles r ON ur.role_id = r.id
-- LEFT JOIN businesses b ON r.business_id = b.id
-- WHERE u.is_deleted = false
-- ORDER BY u.user_type, u.user_identifier;

-- Count summary
-- SELECT
--     (SELECT COUNT(*) FROM businesses WHERE is_deleted = false) as total_businesses,
--     (SELECT COUNT(*) FROM roles WHERE is_deleted = false) as total_roles,
--     (SELECT COUNT(*) FROM users WHERE user_type = 'CUSTOMER' AND is_deleted = false) as total_customers,
--     (SELECT COUNT(*) FROM users WHERE user_type = 'BUSINESS_USER' AND is_deleted = false) as total_business_users,
--     (SELECT COUNT(*) FROM user_roles) as total_role_assignments;
