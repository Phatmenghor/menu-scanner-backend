-- ============================================================
-- MENU SCANNER BACKEND - TEST DATA (CLEAN VERSION)
-- ============================================================
-- ONLY 2 UNSPLASH PREMIUM PHOTOS - Easy Copy/Paste
-- ============================================================

-- 🖼️ PHOTO 1
-- https://plus.unsplash.com/premium_photo-1661432977872-b47a927e1828?q=80&w=870&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D

-- 🖼️ PHOTO 2
-- https://plus.unsplash.com/premium_photo-1681489662994-5e2805750ef1?q=80&w=870&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D

-- ============================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

DO $$ DECLARE
    puid UUID := gen_random_uuid();
    buid UUID := gen_random_uuid();
    cuid UUID := gen_random_uuid();
    bid  UUID := gen_random_uuid();
    t TIMESTAMPTZ := NOW();
    photo1 TEXT := 'https://plus.unsplash.com/premium_photo-1661432977872-b47a927e1828?q=80&w=870&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D';
    photo2 TEXT := 'https://plus.unsplash.com/premium_photo-1681489662994-5e2805750ef1?q=80&w=870&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D';
    role_admin UUID := gen_random_uuid();
    role_business UUID := gen_random_uuid();
    role_customer UUID := gen_random_uuid();
    role_staff UUID := gen_random_uuid();
BEGIN

    -- CLEANUP
    DELETE FROM user_roles WHERE user_id IN (SELECT id FROM users WHERE user_identifier IN ('phatmenghor19@gmail.com','phatmenghor20@gmail.com','phatmenghor21@gmail.com'));
    DELETE FROM users WHERE user_identifier IN ('phatmenghor19@gmail.com','phatmenghor20@gmail.com','phatmenghor21@gmail.com');
    DELETE FROM roles WHERE name IN ('PLATFORM_ADMIN', 'BUSINESS_OWNER', 'CUSTOMER', 'STAFF');

    -- CREATE ROLES
    INSERT INTO roles (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, name, description, business_id, user_type)
    VALUES
        (role_admin, 0, t, t, 'system', 'system', false, NULL, NULL, 'PLATFORM_ADMIN', 'Platform Admin', NULL, 'PLATFORM_USER'),
        (role_business, 0, t, t, 'system', 'system', false, NULL, NULL, 'BUSINESS_OWNER', 'Business Owner', NULL, 'BUSINESS_USER'),
        (role_customer, 0, t, t, 'system', 'system', false, NULL, NULL, 'CUSTOMER', 'Customer', NULL, 'CUSTOMER'),
        (role_staff, 0, t, t, 'system', 'system', false, NULL, NULL, 'STAFF', 'Staff', NULL, 'BUSINESS_USER');

    -- CREATE 3 TEST USERS
    INSERT INTO users (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, user_identifier, email, password, first_name, last_name, phone_number, profile_image_url, user_type, account_status, business_id, position, address, notes, last_login_at, last_active_at, active_sessions_count)
    VALUES
        (puid, 0, t, t, 'system', 'system', false, NULL, NULL, 'phatmenghor19@gmail.com', 'phatmenghor19@gmail.com', '$2a$12$hgZ6m7pwOA8AYv.r7YbuN.Yi8gHh.5NWqpEd2Jn6sgCRyu29a1DEK', 'Phat', 'Menghor', '+855 10 123 4567', photo1, 'PLATFORM_USER', 'ACTIVE', NULL, 'Admin', 'Phnom Penh', 'Platform Admin', t - INTERVAL '2 hours', t - INTERVAL '1 hour', 1),
        (buid, 0, t, t, 'system', 'system', false, NULL, NULL, 'phatmenghor20@gmail.com', 'phatmenghor20@gmail.com', '$2a$12$hgZ6m7pwOA8AYv.r7YbuN.Yi8gHh.5NWqpEd2Jn6sgCRyu29a1DEK', 'Menghor', 'Business', '+855 10 234 5678', photo2, 'BUSINESS_USER', 'ACTIVE', NULL, 'Owner', 'Street 252', 'Business Owner', t - INTERVAL '1 hour', t - INTERVAL '30 minutes', 2),
        (cuid, 0, t, t, 'system', 'system', false, NULL, NULL, 'phatmenghor21@gmail.com', 'phatmenghor21@gmail.com', '$2a$12$hgZ6m7pwOA8AYv.r7YbuN.Yi8gHh.5NWqpEd2Jn6sgCRyu29a1DEK', 'Customer', 'Menghor', '+855 10 345 6789', photo1, 'CUSTOMER', 'ACTIVE', NULL, NULL, 'Phnom Penh', 'Customer', t - INTERVAL '3 hours', t - INTERVAL '45 minutes', 1);

    INSERT INTO user_roles (user_id, role_id) VALUES (puid, role_admin), (buid, role_business), (cuid, role_customer);

    -- CREATE BUSINESS
    INSERT INTO businesses (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, owner_id, name, email, phone, address, description, status, is_subscription_active)
    VALUES (bid, 0, t, t, 'system', 'system', false, NULL, NULL, buid, 'Phat Restaurant', 'phatmenghor20@gmail.com', '+855 23 999 888', 'Street 252, Phnom Penh', 'Best Khmer restaurant', 'ACTIVE', true);

    UPDATE users SET business_id = bid WHERE id = buid;

    -- CREATE BUSINESS SETTINGS
    INSERT INTO business_settings (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, logo_url, banner_url, business_type, opening_time, closing_time, is_open_24_hours, working_days, timezone, currency, language, usd_to_khr_rate, contact_email, contact_phone, whatsapp_number, facebook_url, instagram_url, website_url, primary_color, secondary_color, email_notifications_enabled, sms_notifications_enabled, order_notifications_enabled, tax_rate, service_charge_percentage, min_order_amount, delivery_radius_km, estimated_delivery_time, terms_and_conditions, privacy_policy, refund_policy)
    VALUES (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, photo1, photo2, 'RESTAURANT', '06:00', '23:00', false, 'MONDAY-SUNDAY', 'Asia/Phnom_Penh', 'USD', 'en', 4100.0, 'phatmenghor20@gmail.com', '+855 23 999 888', '+855 10 234 5678', photo1, photo2, photo1, '#FF6B6B', '#FFE66D', true, false, true, 0.0, 10.0, 5.0, 25.0, '30-45 minutes', 'Fresh food guarantee', 'Data protection', 'Full refund');

    -- CREATE 10 CATEGORIES
    INSERT INTO categories (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, name, image_url, status)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, 'Category ' || n, CASE WHEN n % 2 = 0 THEN photo1 ELSE photo2 END, 'ACTIVE'
    FROM GENERATE_SERIES(1, 10) n;

    -- CREATE 10 BRANDS
    INSERT INTO brands (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, name, image_url, description, status)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, 'Brand ' || n, CASE WHEN n % 2 = 0 THEN photo1 ELSE photo2 END, 'Brand description ' || n, 'ACTIVE'
    FROM GENERATE_SERIES(1, 10) n;

    -- CREATE 1000 PRODUCTS (ONLY 2 PHOTOS)
    INSERT INTO products (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, category_id, brand_id, name, description, status, price, promotion_type, promotion_value, promotion_from_date, promotion_to_date, display_price, display_origin_price, display_promotion_type, display_promotion_value, display_promotion_from_date, display_promotion_to_date, has_sizes, has_active_promotion, view_count, favorite_count, main_image_url)
    SELECT
        gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL,
        bid, (SELECT id FROM categories WHERE business_id = bid LIMIT 1), (SELECT id FROM brands WHERE business_id = bid LIMIT 1),
        'Product ' || n, 'Product description ' || n, 'ACTIVE', (15.00 + (n % 80))::NUMERIC,
        NULL, NULL, NULL, NULL, (15.00 + (n % 80))::NUMERIC, (15.00 + (n % 80))::NUMERIC, NULL, NULL, NULL, NULL,
        false, false, (n % 100), (n % 50), CASE WHEN n % 2 = 0 THEN photo1 ELSE photo2 END
    FROM GENERATE_SERIES(1, 1000) n;

    -- CREATE 4000 PRODUCT IMAGES (4 per product - ONLY 2 PHOTOS)
    INSERT INTO product_images (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, product_id, image_url)
    SELECT
        gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL,
        p.id, CASE WHEN (ROW_NUMBER() OVER (PARTITION BY p.id) - 1) % 2 = 0 THEN photo1 ELSE photo2 END
    FROM products p, GENERATE_SERIES(1, 4) img_num
    WHERE p.business_id = bid;

    -- CREATE 100 CUSTOMERS
    INSERT INTO users (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, user_identifier, email, password, first_name, last_name, phone_number, profile_image_url, user_type, account_status, business_id, position, address, notes, last_login_at, last_active_at, active_sessions_count)
    SELECT
        gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL,
        'customer' || n || '@test.com', 'customer' || n || '@test.com',
        '$2a$12$hgZ6m7pwOA8AYv.r7YbuN.Yi8gHh.5NWqpEd2Jn6sgCRyu29a1DEK',
        'Customer', 'User ' || n, '+855 10 234 0' || LPAD(n::TEXT, 3, '0'),
        CASE WHEN n % 2 = 0 THEN photo1 ELSE photo2 END,
        'CUSTOMER', 'ACTIVE', NULL, NULL, 'Phnom Penh', 'Customer ' || n,
        t - (RANDOM() * INTERVAL '14 days'), t - (RANDOM() * INTERVAL '2 days'), 1
    FROM GENERATE_SERIES(1, 100) n;

    INSERT INTO user_roles (user_id, role_id) SELECT u.id, role_customer FROM users u WHERE u.email LIKE 'customer%@test.com' AND NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = u.id);

    -- CREATE 5 DELIVERY OPTIONS
    INSERT INTO delivery_options (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, name, description, image_url, price, status)
    VALUES
        (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, 'Standard', 'Regular delivery', photo1, 2.00, 'ACTIVE'),
        (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, 'Express', 'Fast delivery', photo2, 4.00, 'ACTIVE'),
        (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, 'Scheduled', 'Scheduled delivery', photo1, 2.50, 'ACTIVE'),
        (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, 'Pickup', 'Pickup', photo2, 0.00, 'ACTIVE'),
        (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, 'Dine-in', 'Dine in', photo1, 0.00, 'ACTIVE');

    -- CREATE 5 BANNERS
    INSERT INTO banners (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, image_url, link_url, status)
    VALUES
        (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, photo1, '/menu', 'ACTIVE'),
        (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, photo2, '/menu', 'ACTIVE'),
        (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, photo1, '/menu', 'ACTIVE'),
        (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, photo2, '/menu', 'ACTIVE'),
        (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, photo1, '/menu', 'ACTIVE');

    -- CREATE 100 CARTS
    INSERT INTO carts (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, user_id, business_id)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, u.id, bid
    FROM (SELECT id FROM users WHERE user_type = 'CUSTOMER' ORDER BY RANDOM() LIMIT 100) u;

    -- CREATE 150 CUSTOMER ADDRESSES
    INSERT INTO customer_addresses (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, user_id, village, commune, district, province, country, street_number, house_number, note, latitude, longitude, is_default)
    SELECT
        gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL,
        (SELECT id FROM users WHERE user_type = 'CUSTOMER' ORDER BY RANDOM() LIMIT 1),
        'Village ' || n, 'Commune ' || n, 'District ' || n, 'Phnom Penh', 'Cambodia',
        'Street ' || n, 'House ' || n, 'Apt ' || n, 11.5564, 104.9282, n % 3 = 1
    FROM GENERATE_SERIES(1, 150) n;

    RAISE NOTICE 'Test data created successfully!';
END $$;

SELECT 'Done! ✅' as status;
