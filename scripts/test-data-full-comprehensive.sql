-- ============================================================
-- MASSIVE TEST DATA - 100x EXPANSION
-- ============================================================
-- 2000 Platform Admins | 3000 Business Users | 6000 Customers
-- HR Features Fully Populated | ONLY 2 Unsplash Premium Photos
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

DO $$ DECLARE
    t TIMESTAMPTZ := NOW();
    photo1 TEXT := 'https://plus.unsplash.com/premium_photo-1661432977872-b47a927e1828?q=80&w=870&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D';
    photo2 TEXT := 'https://plus.unsplash.com/premium_photo-1681489662994-5e2805750ef1?q=80&w=870&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D';
    role_admin UUID := gen_random_uuid();
    role_business UUID := gen_random_uuid();
    role_customer UUID := gen_random_uuid();
    role_staff UUID := gen_random_uuid();
    plan1 UUID := gen_random_uuid();
    plan2 UUID := gen_random_uuid();
    plan3 UUID := gen_random_uuid();
BEGIN

    -- ========== CLEANUP ALL TABLES ==========
    DELETE FROM order_status_history;
    DELETE FROM order_items;
    DELETE FROM order_payments;
    DELETE FROM orders;
    DELETE FROM order_process_statuses;
    DELETE FROM cart_items;
    DELETE FROM carts;
    DELETE FROM product_favorites;
    DELETE FROM product_images;
    DELETE FROM product_sizes;
    DELETE FROM products;
    DELETE FROM banners;
    DELETE FROM delivery_options;
    DELETE FROM business_exchange_rates;
    DELETE FROM subscriptions;
    DELETE FROM subscription_plans;
    DELETE FROM customer_addresses;
    DELETE FROM attendance_check_ins;
    DELETE FROM attendances;
    DELETE FROM leaves;
    DELETE FROM work_schedules;
    DELETE FROM business_settings;
    DELETE FROM categories;
    DELETE FROM brands;
    DELETE FROM user_roles;
    DELETE FROM businesses;
    DELETE FROM users WHERE user_type IN ('PLATFORM_USER', 'BUSINESS_USER', 'CUSTOMER');
    DELETE FROM roles WHERE user_type IN ('PLATFORM_USER', 'BUSINESS_USER', 'CUSTOMER', 'STAFF');
    DELETE FROM exchange_rates;
    DELETE FROM reference_counters;

    -- ========== ROLES ==========
    INSERT INTO roles (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, name, description, business_id, user_type)
    VALUES
        (role_admin, 0, t, t, 'system', 'system', false, NULL, NULL, 'PLATFORM_ADMIN', 'Platform Administrator', NULL, 'PLATFORM_USER'),
        (role_business, 0, t, t, 'system', 'system', false, NULL, NULL, 'BUSINESS_OWNER', 'Business Owner', NULL, 'BUSINESS_USER'),
        (role_customer, 0, t, t, 'system', 'system', false, NULL, NULL, 'CUSTOMER', 'Customer', NULL, 'CUSTOMER'),
        (role_staff, 0, t, t, 'system', 'system', false, NULL, NULL, 'STAFF', 'Business Staff', NULL, 'BUSINESS_USER');

    -- ========== 2000 PLATFORM ADMIN USERS ==========
    INSERT INTO users (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, user_identifier, email, password, first_name, last_name, phone_number, profile_image_url, user_type, account_status, business_id, position, address, notes, last_login_at, last_active_at, active_sessions_count)
    SELECT
        gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL,
        'admin' || n || '@platform.com', 'admin' || n || '@platform.com',
        '$2a$12$hgZ6m7pwOA8AYv.r7YbuN.Yi8gHh.5NWqpEd2Jn6sgCRyu29a1DEK',
        'Admin' || n, 'User' || n, '+855 10 100 ' || LPAD((n % 10000)::TEXT, 4, '0'),
        CASE WHEN n % 2 = 0 THEN photo1 ELSE photo2 END,
        'PLATFORM_USER', 'ACTIVE', NULL, 'Platform Admin',
        'Phnom Penh', 'Platform Admin ' || n,
        t - (RANDOM() * INTERVAL '90 days'), t - (RANDOM() * INTERVAL '7 days'), 1
    FROM GENERATE_SERIES(1, 2000) n;

    INSERT INTO user_roles (user_id, role_id)
    SELECT u.id, role_admin FROM users u WHERE u.user_type = 'PLATFORM_USER' AND NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = u.id);

    -- ========== SUBSCRIPTION PLANS ==========
    INSERT INTO subscription_plans (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, name, description, price, duration_days, status)
    VALUES
        (plan1, 0, t, t, 'system', 'system', false, NULL, NULL, 'Basic Plan', 'Basic features', 99.99, 30, 'PUBLIC'),
        (plan2, 0, t, t, 'system', 'system', false, NULL, NULL, 'Pro Plan', 'Advanced features', 299.99, 365, 'PUBLIC'),
        (plan3, 0, t, t, 'system', 'system', false, NULL, NULL, 'Enterprise', 'Enterprise features', 999.99, 365, 'PUBLIC');

    -- ========== 100 BUSINESSES ==========
    INSERT INTO businesses (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, owner_id, name, email, phone, address, description, status, is_subscription_active)
    SELECT
        gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL,
        (SELECT u.id FROM users u WHERE u.user_type = 'PLATFORM_USER' ORDER BY RANDOM() LIMIT 1),
        'Business ' || n, 'business' || n || '@test.com', '+855 23 ' || LPAD((n % 10000)::TEXT, 4, '0'),
        'Address ' || n || ', Phnom Penh', 'Business description ' || n,
        'ACTIVE', CASE WHEN n % 2 = 0 THEN true ELSE false END
    FROM GENERATE_SERIES(1, 100) n;

    -- ========== BUSINESS SETTINGS (for each business) ==========
    INSERT INTO business_settings (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, logo_url, banner_url, business_type, opening_time, closing_time, is_open_24_hours, working_days, timezone, currency, language, usd_to_khr_rate, contact_email, contact_phone, whatsapp_number, facebook_url, instagram_url, website_url, primary_color, secondary_color, email_notifications_enabled, sms_notifications_enabled, order_notifications_enabled, tax_rate, service_charge_percentage, min_order_amount, delivery_radius_km, estimated_delivery_time, terms_and_conditions, privacy_policy, refund_policy)
    SELECT
        gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL,
        b.id, photo1, photo2, 'RESTAURANT', '06:00', '23:00', false, 'MONDAY-SUNDAY',
        'Asia/Phnom_Penh', 'USD', 'en', 4100.0, b.email, b.phone, '+855 10 100 0001',
        photo1, photo2, photo1, '#FF6B6B', '#FFE66D', true, false, true,
        0.0, 10.0, 5.0, 25.0, '30-45 minutes', 'Fresh guarantee', 'Data protection', 'Full refund'
    FROM businesses b;

    -- ========== SUBSCRIPTIONS for businesses ==========
    INSERT INTO subscriptions (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, plan_id, start_date, end_date, auto_renew)
    SELECT
        gen_random_uuid(), 0, t - INTERVAL '6 months', t, 'system', 'system', false, NULL, NULL,
        b.id,
        CASE (ROW_NUMBER() OVER (ORDER BY b.id) % 3) WHEN 0 THEN plan1 WHEN 1 THEN plan2 ELSE plan3 END,
        t - INTERVAL '6 months', t + INTERVAL '6 months', true
    FROM businesses b;

    -- ========== BUSINESS ROLES (12 roles per business) ==========
    INSERT INTO roles (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, name, description, business_id, user_type)
    SELECT
        gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL,
        CASE r WHEN 1 THEN 'Manager' WHEN 2 THEN 'Chef' WHEN 3 THEN 'Sous Chef' WHEN 4 THEN 'Waiter' WHEN 5 THEN 'Cashier' WHEN 6 THEN 'Delivery Driver' WHEN 7 THEN 'Kitchen Staff' WHEN 8 THEN 'Supervisor' WHEN 9 THEN 'Accountant' WHEN 10 THEN 'Marketing' WHEN 11 THEN 'HR Officer' WHEN 12 THEN 'Customer Service' END,
        'Business Role ' || r, b.id, 'BUSINESS_USER'
    FROM businesses b, GENERATE_SERIES(1, 12) r;

    -- ========== 3000 BUSINESS USERS (30 per business) ==========
    INSERT INTO users (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, user_identifier, email, password, first_name, last_name, phone_number, profile_image_url, user_type, account_status, business_id, position, address, notes, last_login_at, last_active_at, active_sessions_count)
    SELECT
        gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL,
        'staff' || b_idx || '-' || s || '@business.com', 'staff' || b_idx || '-' || s || '@business.com',
        '$2a$12$hgZ6m7pwOA8AYv.r7YbuN.Yi8gHh.5NWqpEd2Jn6sgCRyu29a1DEK',
        'Staff', 'Member ' || b_idx || '-' || s, '+855 10 200 ' || LPAD((s)::TEXT, 4, '0'),
        CASE WHEN s % 2 = 0 THEN photo1 ELSE photo2 END,
        'BUSINESS_USER', 'ACTIVE', (SELECT id FROM businesses ORDER BY id OFFSET (b_idx - 1) LIMIT 1),
        'Position ' || s, 'Business Address', 'Staff ' || b_idx || '-' || s,
        t - (RANDOM() * INTERVAL '30 days'), t - (RANDOM() * INTERVAL '2 days'), 1
    FROM GENERATE_SERIES(1, 100) b_idx, GENERATE_SERIES(1, 30) s;

    INSERT INTO user_roles (user_id, role_id)
    SELECT u.id, r.id FROM users u JOIN businesses b ON u.business_id = b.id JOIN roles r ON r.business_id = b.id WHERE u.user_type = 'BUSINESS_USER' AND NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = u.id) LIMIT 3000;

    -- ========== 6000 CUSTOMERS ==========
    INSERT INTO users (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, user_identifier, email, password, first_name, last_name, phone_number, profile_image_url, user_type, account_status, business_id, position, address, notes, last_login_at, last_active_at, active_sessions_count)
    SELECT
        gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL,
        'customer' || n || '@test.com', 'customer' || n || '@test.com',
        '$2a$12$hgZ6m7pwOA8AYv.r7YbuN.Yi8gHh.5NWqpEd2Jn6sgCRyu29a1DEK',
        'Customer' || n, 'User' || n, '+855 10 300 ' || LPAD((n % 10000)::TEXT, 4, '0'),
        CASE WHEN n % 2 = 0 THEN photo1 ELSE photo2 END,
        'CUSTOMER', 'ACTIVE', NULL, NULL, 'Phnom Penh', 'Customer ' || n,
        t - (RANDOM() * INTERVAL '60 days'), t - (RANDOM() * INTERVAL '5 days'), 1
    FROM GENERATE_SERIES(1, 6000) n;

    INSERT INTO user_roles (user_id, role_id)
    SELECT u.id, role_customer FROM users u WHERE u.user_type = 'CUSTOMER' AND NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = u.id);

    -- ========== 100 CATEGORIES (per business) ==========
    INSERT INTO categories (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, name, image_url, status)
    SELECT
        gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL,
        b.id, 'Category ' || c, CASE WHEN c % 2 = 0 THEN photo1 ELSE photo2 END, 'ACTIVE'
    FROM businesses b, GENERATE_SERIES(1, 100) c;

    -- ========== 100 BRANDS (per business) ==========
    INSERT INTO brands (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, name, image_url, description, status)
    SELECT
        gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL,
        b.id, 'Brand ' || br, CASE WHEN br % 2 = 0 THEN photo1 ELSE photo2 END, 'Brand description ' || br, 'ACTIVE'
    FROM businesses b, GENERATE_SERIES(1, 100) br;

    -- ========== 10000 PRODUCTS (100 per business) ==========
    INSERT INTO products (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, category_id, brand_id, name, description, status, price, promotion_type, promotion_value, promotion_from_date, promotion_to_date, display_price, display_origin_price, display_promotion_type, display_promotion_value, display_promotion_from_date, display_promotion_to_date, has_sizes, has_active_promotion, view_count, favorite_count, main_image_url)
    SELECT
        gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL,
        b.id, (SELECT id FROM categories WHERE business_id = b.id ORDER BY RANDOM() LIMIT 1),
        (SELECT id FROM brands WHERE business_id = b.id ORDER BY RANDOM() LIMIT 1),
        'Product ' || p, 'Product description ' || p, 'ACTIVE', (15.00 + (p % 80))::NUMERIC,
        CASE WHEN p % 3 = 0 THEN 'PERCENTAGE' ELSE NULL END, CASE WHEN p % 3 = 0 THEN 10 ELSE NULL END,
        CASE WHEN p % 3 = 0 THEN t - INTERVAL '5 days' ELSE NULL END, CASE WHEN p % 3 = 0 THEN t + INTERVAL '30 days' ELSE NULL END,
        (15.00 + (p % 80))::NUMERIC, (15.00 + (p % 80))::NUMERIC,
        CASE WHEN p % 3 = 0 THEN 'PERCENTAGE' ELSE NULL END, CASE WHEN p % 3 = 0 THEN 10 ELSE NULL END,
        CASE WHEN p % 3 = 0 THEN t - INTERVAL '5 days' ELSE NULL END, CASE WHEN p % 3 = 0 THEN t + INTERVAL '30 days' ELSE NULL END,
        CASE WHEN p % 5 = 0 THEN true ELSE false END, CASE WHEN p % 3 = 0 THEN true ELSE false END,
        (p % 500), (p % 100), CASE WHEN p % 2 = 0 THEN photo1 ELSE photo2 END
    FROM businesses b, GENERATE_SERIES(1, 100) p;

    -- ========== 40000 PRODUCT IMAGES (4 per product) ==========
    INSERT INTO product_images (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, product_id, image_url)
    SELECT
        gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL,
        p.id, CASE WHEN (ROW_NUMBER() OVER (PARTITION BY p.id) - 1) % 2 = 0 THEN photo1 ELSE photo2 END
    FROM products p, GENERATE_SERIES(1, 4) img_num;

    -- ========== PRODUCT SIZES ==========
    INSERT INTO product_sizes (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, product_id, name, price, promotion_type, promotion_value, promotion_from_date, promotion_to_date)
    SELECT
        gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL,
        p.id, CASE s WHEN 1 THEN 'Small' WHEN 2 THEN 'Medium' WHEN 3 THEN 'Large' WHEN 4 THEN 'Extra Large' END,
        (p.price + (s * 2))::NUMERIC, p.promotion_type, p.promotion_value, p.promotion_from_date, p.promotion_to_date
    FROM (SELECT id, price, promotion_type, promotion_value, promotion_from_date, promotion_to_date FROM products WHERE has_sizes = true) p,
    GENERATE_SERIES(1, 4) s;

    -- ========== 15000 CUSTOMER ADDRESSES ==========
    INSERT INTO customer_addresses (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, user_id, village, commune, district, province, country, street_number, house_number, note, latitude, longitude, is_default)
    SELECT
        gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL,
        (SELECT id FROM users WHERE user_type = 'CUSTOMER' ORDER BY RANDOM() LIMIT 1),
        'Village ' || a, 'Commune ' || a, 'District ' || a, 'Phnom Penh', 'Cambodia',
        'Street ' || a, 'House ' || a, 'Apt ' || a, 11.5564 + (a::NUMERIC / 1000), 104.9282 + (a::NUMERIC / 1000), a % 3 = 1
    FROM GENERATE_SERIES(1, 15000) a;

    -- ========== 5 DELIVERY OPTIONS (per business) ==========
    INSERT INTO delivery_options (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, name, description, image_url, price, status)
    SELECT
        gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL,
        b.id,
        CASE d WHEN 1 THEN 'Standard Delivery' WHEN 2 THEN 'Express Delivery' WHEN 3 THEN 'Scheduled Delivery' WHEN 4 THEN 'Pickup' WHEN 5 THEN 'Dine-in' END,
        CASE d WHEN 1 THEN 'Regular delivery' WHEN 2 THEN 'Fast delivery' WHEN 3 THEN 'Scheduled' WHEN 4 THEN 'Pickup' WHEN 5 THEN 'Dine in' END,
        CASE WHEN d % 2 = 0 THEN photo1 ELSE photo2 END,
        CASE d WHEN 1 THEN 2.00 WHEN 2 THEN 4.00 WHEN 3 THEN 2.50 WHEN 4 THEN 0.00 WHEN 5 THEN 0.00 END, 'ACTIVE'
    FROM businesses b, GENERATE_SERIES(1, 5) d;

    -- ========== 10 BANNERS (per business) ==========
    INSERT INTO banners (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, image_url, link_url, status)
    SELECT
        gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL,
        b.id, CASE WHEN bn % 2 = 0 THEN photo1 ELSE photo2 END, '/menu', 'ACTIVE'
    FROM businesses b, GENERATE_SERIES(1, 10) bn;

    -- ========== 6000 CARTS ==========
    INSERT INTO carts (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, user_id, business_id)
    SELECT
        gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL,
        u.id,
        (SELECT id FROM businesses ORDER BY RANDOM() LIMIT 1)
    FROM (SELECT id FROM users WHERE user_type = 'CUSTOMER' ORDER BY RANDOM() LIMIT 6000) u;

    -- ========== 30000 CART ITEMS ==========
    INSERT INTO cart_items (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, cart_id, product_id, product_size_id, quantity)
    SELECT
        gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL,
        (SELECT id FROM carts ORDER BY RANDOM() LIMIT 1), (SELECT id FROM products ORDER BY RANDOM() LIMIT 1), NULL, (1 + (ci % 5))
    FROM GENERATE_SERIES(1, 30000) ci;

    -- ========== ORDER PROCESS STATUSES ==========
    INSERT INTO order_process_statuses (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, name, description, status)
    SELECT
        gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL,
        b.id,
        CASE s WHEN 1 THEN 'Pending' WHEN 2 THEN 'Confirmed' WHEN 3 THEN 'Preparing' WHEN 4 THEN 'Ready' WHEN 5 THEN 'In Delivery' WHEN 6 THEN 'Delivered' WHEN 7 THEN 'Completed' WHEN 8 THEN 'Cancelled' WHEN 9 THEN 'Refunded' WHEN 10 THEN 'Failed' END,
        CASE s WHEN 1 THEN 'Order received' WHEN 2 THEN 'Order confirmed' WHEN 3 THEN 'Kitchen preparing' WHEN 4 THEN 'Ready for delivery' WHEN 5 THEN 'Out for delivery' WHEN 6 THEN 'Successfully delivered' WHEN 7 THEN 'Order completed' WHEN 8 THEN 'Order cancelled' WHEN 9 THEN 'Order refunded' WHEN 10 THEN 'Delivery failed' END,
        'ACTIVE'
    FROM businesses b, GENERATE_SERIES(1, 10) s;

    -- ========== 20000 ORDERS WITH ITEMS ==========
    INSERT INTO reference_counters (entity_type, counter_date, counter_value) VALUES ('ORDER', CURRENT_DATE, 0) ON CONFLICT (entity_type, counter_date) DO NOTHING;

    FOR n IN 1..20000 LOOP
        DECLARE
            order_id UUID := gen_random_uuid();
            customer_id UUID;
            product_id UUID;
            status_id UUID;
            business_id UUID;
            subtotal NUMERIC;
            discount NUMERIC;
            delivery NUMERIC;
            tax NUMERIC;
            total NUMERIC;
            counter BIGINT;
        BEGIN
            SELECT id INTO customer_id FROM users WHERE user_type = 'CUSTOMER' ORDER BY RANDOM() LIMIT 1;
            SELECT id INTO business_id FROM businesses ORDER BY RANDOM() LIMIT 1;
            SELECT id FROM products p WHERE p.business_id = business_id ORDER BY RANDOM() LIMIT 1 INTO product_id;
            SELECT id INTO status_id FROM order_process_statuses ops WHERE ops.business_id = business_id AND ops.name = 'Pending' LIMIT 1;

            UPDATE reference_counters SET counter_value = counter_value + 1 WHERE entity_type = 'ORDER' AND counter_date = CURRENT_DATE;
            SELECT counter_value INTO counter FROM reference_counters WHERE entity_type = 'ORDER' AND counter_date = CURRENT_DATE;

            subtotal := (20.00 + (n % 100))::NUMERIC;
            discount := (n % 5)::NUMERIC;
            delivery := (2.00 + (n % 8))::NUMERIC;
            tax := (n % 3)::NUMERIC;
            total := subtotal - discount + delivery + tax;

            INSERT INTO orders (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, customer_id, order_number, order_process_status_name, delivery_address_snapshot, delivery_option_snapshot, subtotal, discount_amount, delivery_fee, tax_amount, total_amount, payment_method, payment_status, customer_note, business_note, confirmed_at, completed_at)
            VALUES (order_id, 0, t - (RANDOM() * INTERVAL '120 days'), t, 'system', 'system', false, NULL, NULL, business_id, customer_id, 'ORD-' || TO_CHAR(NOW(), 'YYYYMMDD') || '-' || LPAD(counter::TEXT, 6, '0'), 'Pending', '{"village":"Village","commune":"Commune","district":"District","province":"Phnom Penh","street":"Street","house":"House"}', '{"name":"Standard","price":2.00}', subtotal, discount, delivery, tax, total, CASE (n % 4) WHEN 0 THEN 'CASH' WHEN 1 THEN 'BANK_TRANSFER' ELSE 'ONLINE' END, CASE (n % 3) WHEN 0 THEN 'UNPAID' WHEN 1 THEN 'PAID' ELSE 'COMPLETED' END, 'Note ' || n, 'Business note ' || n, CASE WHEN n % 2 = 0 THEN t - (RANDOM() * INTERVAL '100 days') ELSE NULL END, CASE WHEN n % 3 = 2 THEN t - (RANDOM() * INTERVAL '60 days') ELSE NULL END);

            -- Order items (3-5 per order)
            FOR i IN 1..3 LOOP
                INSERT INTO order_items (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, order_id, product_id, product_size_id, product_name, product_image_url, size_name, current_price, final_price, unit_price, quantity, total_price, has_promotion)
                VALUES (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, order_id, product_id, NULL, 'Item', CASE WHEN i % 2 = 0 THEN photo1 ELSE photo2 END, CASE WHEN i % 2 = 0 THEN 'Medium' ELSE 'Large' END, (10 + i)::NUMERIC, (10 + i)::NUMERIC, (10 + i)::NUMERIC, i, (i * (10 + i))::NUMERIC, n % 3 = 0);
            END LOOP;

            -- Order status history
            INSERT INTO order_status_history (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, order_id, order_process_status_id, note, changed_by_user_id)
            VALUES (gen_random_uuid(), 0, t - (RANDOM() * INTERVAL '120 days'), t, 'system', 'system', false, NULL, NULL, order_id, status_id, 'Created', NULL);

            -- Order payment
            INSERT INTO order_payments (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, order_id, payment_reference, subtotal, discount_amount, delivery_fee, tax_amount, total_amount, payment_method, status, customer_payment_method)
            VALUES (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, business_id, order_id, 'PAY-' || LPAD(n::TEXT, 8, '0'), subtotal, discount, delivery, tax, total, CASE (n % 4) WHEN 0 THEN 'CASH' WHEN 1 THEN 'BANK_TRANSFER' ELSE 'ONLINE' END, CASE (n % 3) WHEN 0 THEN 'PENDING' WHEN 1 THEN 'FAILED' ELSE 'COMPLETED' END, 'Cash');
        END;
    END LOOP;

    -- ========== 10000 PRODUCT FAVORITES ==========
    INSERT INTO product_favorites (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, user_id, product_id)
    SELECT
        gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL,
        (SELECT id FROM users WHERE user_type = 'CUSTOMER' ORDER BY RANDOM() LIMIT 1),
        (SELECT id FROM products ORDER BY RANDOM() LIMIT 1)
    FROM GENERATE_SERIES(1, 10000) pf;

    -- ========== HR: WORK SCHEDULES (1000 per business for staff) ==========
    INSERT INTO work_schedules (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, user_id, date, start_time, end_time, type)
    SELECT
        gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL,
        u.business_id, u.id,
        (t + (INTERVAL '1 day' * (ws % 30)))::DATE,
        CASE WHEN ws % 3 = 0 THEN '06:00' WHEN ws % 3 = 1 THEN '10:00' ELSE '14:00' END,
        CASE WHEN ws % 3 = 0 THEN '14:00' WHEN ws % 3 = 1 THEN '18:00' ELSE '22:00' END,
        CASE WHEN ws % 2 = 0 THEN 'MORNING' ELSE 'EVENING' END
    FROM users u, GENERATE_SERIES(1, 10) ws
    WHERE u.user_type = 'BUSINESS_USER';

    -- ========== HR: ATTENDANCE (5000 records) ==========
    INSERT INTO attendances (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, user_id, date, total_hours, status)
    SELECT
        gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL,
        u.business_id, u.id, (t - (INTERVAL '1 day' * (att % 60)))::DATE, (7 + (att % 2))::NUMERIC, CASE WHEN att % 10 = 0 THEN 'ABSENT' ELSE 'PRESENT' END
    FROM users u, GENERATE_SERIES(1, 5) att
    WHERE u.user_type = 'BUSINESS_USER';

    -- ========== HR: ATTENDANCE CHECK-IN (10000 records) ==========
    INSERT INTO attendance_check_ins (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, user_id, check_in_time, check_out_time, notes)
    SELECT
        gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL,
        u.business_id, u.id,
        (t - (INTERVAL '1 day' * (aci % 60)))::TIMESTAMP + ('06:' || LPAD((aci % 60)::TEXT, 2, '0') || ':00')::TIME,
        (t - (INTERVAL '1 day' * (aci % 60)))::TIMESTAMP + ('14:' || LPAD((aci % 60)::TEXT, 2, '0') || ':00')::TIME,
        'Check-in ' || aci
    FROM users u, GENERATE_SERIES(1, 10) aci
    WHERE u.user_type = 'BUSINESS_USER';

    -- ========== HR: LEAVE (2000 records) ==========
    INSERT INTO leaves (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, user_id, start_date, end_date, reason, status, approved_by_user_id, approved_at)
    SELECT
        gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL,
        u.business_id, u.id,
        (t + (INTERVAL '1 day' * (lv % 365)))::DATE,
        (t + (INTERVAL '1 day' * ((lv % 365) + 3)))::DATE,
        'Leave reason ' || lv,
        CASE WHEN lv % 4 = 0 THEN 'PENDING' WHEN lv % 4 = 1 THEN 'APPROVED' WHEN lv % 4 = 2 THEN 'REJECTED' ELSE 'CANCELLED' END,
        CASE WHEN lv % 2 = 0 THEN (SELECT id FROM users uu WHERE uu.business_id = u.business_id AND uu.user_type = 'BUSINESS_USER' ORDER BY RANDOM() LIMIT 1) ELSE NULL END,
        CASE WHEN lv % 2 = 0 THEN t ELSE NULL END
    FROM users u, GENERATE_SERIES(1, 2) lv
    WHERE u.user_type = 'BUSINESS_USER';

    -- ========== EXCHANGE RATES ==========
    INSERT INTO exchange_rates (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, usd_to_khr_rate, is_active, notes)
    VALUES (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, 4100.0, true, 'Standard rate');

    -- ========== BUSINESS EXCHANGE RATES ==========
    INSERT INTO business_exchange_rates (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, usd_to_khr_rate, usd_to_thb_rate, usd_to_cny_rate, usd_to_vnd_rate, is_active, notes)
    SELECT
        gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL,
        b.id, 4105.0 + (ber % 10)::NUMERIC, 35.45 + (ber % 1)::NUMERIC, 7.25 + (ber % 1)::NUMERIC, 24500.0 + (ber % 100)::NUMERIC, true, 'Custom rates ' || ber
    FROM businesses b, GENERATE_SERIES(1, 2) ber;

    RAISE NOTICE '✅ MASSIVE TEST DATA GENERATED!';
    RAISE NOTICE '✓ 2000 Platform Admins | ✓ 3000 Business Users | ✓ 6000 Customers';
    RAISE NOTICE '✓ 100 Businesses | ✓ 10000 Products | ✓ 40000 Product Images';
    RAISE NOTICE '✓ 20000 Orders | ✓ 60000 Order Items | ✓ 6000 Carts | ✓ 30000 Cart Items';
    RAISE NOTICE '✓ 15000 Customer Addresses | ✓ 10000 Product Favorites';
    RAISE NOTICE '✓ 30000+ Work Schedules | ✓ 15000+ Attendance Records | ✓ 10000+ Check-ins';
    RAISE NOTICE '✓ 2000+ Leave Records | ✓ Exchange Rates';
    RAISE NOTICE '✓ ONLY 2 Unsplash Premium Photos used throughout!';

END $$;

SELECT 'DONE ✅ MASSIVE TEST DATA POPULATED!' as status;
