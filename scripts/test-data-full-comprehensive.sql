-- ============================================================
-- MASSIVE TEST DATA WITH 3 KEY USERS
-- ============================================================
-- 3 Key Users: platform, business, customer (LOTS OF DATA)
-- Plus: 2000 Platform Admins | 3000 Business Users | 6000 Customers
-- All HR Features | Only 2 Photos
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

DO $$ DECLARE
    t TIMESTAMPTZ := NOW();
    photo1 TEXT := 'https://plus.unsplash.com/premium_photo-1673002094195-f18084be89ce?q=80&w=1200&auto=format&fit=crop';
    photo2 TEXT := 'https://plus.unsplash.com/premium_photo-1661964071015-d97428970584?q=80&w=1200&auto=format&fit=crop';

    role_admin UUID := gen_random_uuid();
    role_business UUID := gen_random_uuid();
    role_customer UUID := gen_random_uuid();
    plan1 UUID := gen_random_uuid();
    plan2 UUID := gen_random_uuid();
    plan3 UUID := gen_random_uuid();

    -- KEY USERS
    platform_user_id UUID := gen_random_uuid();
    business_user_id UUID := gen_random_uuid();
    customer_user_id UUID := gen_random_uuid();
    key_business_id UUID := gen_random_uuid();
BEGIN

    TRUNCATE TABLE order_status_history CASCADE;
    TRUNCATE TABLE order_items CASCADE;
    TRUNCATE TABLE order_payments CASCADE;
    TRUNCATE TABLE orders CASCADE;
    TRUNCATE TABLE order_process_statuses CASCADE;
    TRUNCATE TABLE cart_items CASCADE;
    TRUNCATE TABLE carts CASCADE;
    TRUNCATE TABLE product_favorites CASCADE;
    TRUNCATE TABLE product_images CASCADE;
    TRUNCATE TABLE product_sizes CASCADE;
    TRUNCATE TABLE products CASCADE;
    TRUNCATE TABLE banners CASCADE;
    TRUNCATE TABLE delivery_options CASCADE;
    TRUNCATE TABLE business_exchange_rates CASCADE;
    TRUNCATE TABLE subscriptions CASCADE;
    TRUNCATE TABLE subscription_plans CASCADE;
    TRUNCATE TABLE customer_addresses CASCADE;
    TRUNCATE TABLE attendance_check_ins CASCADE;
    TRUNCATE TABLE attendances CASCADE;
    TRUNCATE TABLE leaves CASCADE;
    TRUNCATE TABLE work_schedules CASCADE;
    TRUNCATE TABLE business_settings CASCADE;
    TRUNCATE TABLE categories CASCADE;
    TRUNCATE TABLE brands CASCADE;
    TRUNCATE TABLE user_roles CASCADE;
    TRUNCATE TABLE businesses CASCADE;
    TRUNCATE TABLE users CASCADE;
    TRUNCATE TABLE roles CASCADE;
    TRUNCATE TABLE exchange_rates CASCADE;
    TRUNCATE TABLE reference_counters CASCADE;

    -- ========== ROLES ==========
    INSERT INTO roles (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, name, description, business_id, user_type)
    VALUES
        (role_admin, 0, t, t, 'system', 'system', false, NULL, NULL, 'PLATFORM_ADMIN', 'Platform Administrator', NULL, 'PLATFORM_USER'),
        (role_business, 0, t, t, 'system', 'system', false, NULL, NULL, 'BUSINESS_OWNER', 'Business Owner', NULL, 'BUSINESS_USER'),
        (role_customer, 0, t, t, 'system', 'system', false, NULL, NULL, 'CUSTOMER', 'Customer', NULL, 'CUSTOMER');

    -- ========== KEY USER 1: PLATFORM ADMIN ==========
    INSERT INTO users (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, user_identifier, email, password, first_name, last_name, phone_number, profile_image_url, user_type, account_status, business_id, position, address, notes, last_login_at, last_active_at, active_sessions_count)
    VALUES
        (platform_user_id, 0, t, t, 'system', 'system', false, NULL, NULL, 'phatmenghor19@gmail.com', 'phatmenghor19@gmail.com', '$2a$12$hgZ6m7pwOA8AYv.r7YbuN.Yi8gHh.5NWqpEd2Jn6sgCRyu29a1DEK', 'Platform', 'Admin', '+855 10 100 0001', photo1, 'PLATFORM_USER', 'ACTIVE', NULL, 'Platform Admin', 'Phnom Penh', 'Key Platform Admin User', t - INTERVAL '1 day', t, 1);

    INSERT INTO user_roles (user_id, role_id) VALUES (platform_user_id, role_admin);

    -- ========== SUBSCRIPTION PLANS ==========
    INSERT INTO subscription_plans (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, name, description, price, duration_days, status)
    VALUES
        (plan1, 0, t, t, 'system', 'system', false, NULL, NULL, 'Basic Plan', 'Basic features', 99.99, 30, 'PUBLIC'),
        (plan2, 0, t, t, 'system', 'system', false, NULL, NULL, 'Pro Plan', 'Advanced features', 299.99, 365, 'PUBLIC'),
        (plan3, 0, t, t, 'system', 'system', false, NULL, NULL, 'Enterprise', 'Enterprise features', 999.99, 365, 'PUBLIC');

    -- ========== KEY BUSINESS FOR BUSINESS USER ==========
    INSERT INTO businesses (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, owner_id, name, email, phone, address, description, status, is_subscription_active)
    VALUES
        (key_business_id, 0, t, t, 'system', 'system', false, NULL, NULL, platform_user_id, 'Phatmenghor Test Business', 'business@test.com', '+855 23 1234567', 'Phnom Penh, Cambodia', 'Main test business for development', 'ACTIVE', true);

    -- ========== BUSINESS SETTINGS FOR KEY BUSINESS ==========
    INSERT INTO business_settings (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, logo_url, banner_url, business_type, opening_time, closing_time, is_open_24_hours, working_days, timezone, currency, language, usd_to_khr_rate, contact_email, contact_phone, whatsapp_number, facebook_url, instagram_url, website_url, primary_color, secondary_color, email_notifications_enabled, sms_notifications_enabled, order_notifications_enabled, tax_rate, service_charge_percentage, min_order_amount, delivery_radius_km, estimated_delivery_time, terms_and_conditions, privacy_policy, refund_policy)
    VALUES
        (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, key_business_id, photo1, photo2, 'RESTAURANT', '06:00', '23:00', false, 'MONDAY-SUNDAY', 'Asia/Phnom_Penh', 'USD', 'en', 4100.0, 'business@test.com', '+855 23 1234567', '+855 10 100 0001', photo1, photo2, photo1, '#FF6B6B', '#FFE66D', true, false, true, 0.0, 10.0, 5.0, 25.0, '30-45 minutes', 'Fresh guarantee', 'Data protection', 'Full refund');

    -- ========== SUBSCRIPTION FOR KEY BUSINESS ==========
    INSERT INTO subscriptions (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, plan_id, start_date, end_date, auto_renew)
    VALUES
        (gen_random_uuid(), 0, t - INTERVAL '6 months', t, 'system', 'system', false, NULL, NULL, key_business_id, plan2, t - INTERVAL '6 months', t + INTERVAL '6 months', true);

    -- ========== KEY USER 2: BUSINESS USER ==========
    INSERT INTO users (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, user_identifier, email, password, first_name, last_name, phone_number, profile_image_url, user_type, account_status, business_id, position, address, notes, last_login_at, last_active_at, active_sessions_count)
    VALUES
        (business_user_id, 0, t, t, 'system', 'system', false, NULL, NULL, 'phatmenghor20@gmail.com', 'phatmenghor20@gmail.com', '$2a$12$hgZ6m7pwOA8AYv.r7YbuN.Yi8gHh.5NWqpEd2Jn6sgCRyu29a1DEK', 'Business', 'Manager', '+855 10 200 0001', photo2, 'BUSINESS_USER', 'ACTIVE', key_business_id, 'Business Manager', 'Phnom Penh', 'Key Business User', t - INTERVAL '2 days', t, 1);

    -- ========== KEY USER 3: CUSTOMER ==========
    INSERT INTO users (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, user_identifier, email, password, first_name, last_name, phone_number, profile_image_url, user_type, account_status, business_id, position, address, notes, last_login_at, last_active_at, active_sessions_count)
    VALUES
        (customer_user_id, 0, t, t, 'system', 'system', false, NULL, NULL, 'phatmenghor21@gmail.com', 'phatmenghor21@gmail.com', '$2a$12$hgZ6m7pwOA8AYv.r7YbuN.Yi8gHh.5NWqpEd2Jn6sgCRyu29a1DEK', 'Customer', 'User', '+855 10 300 0001', photo1, 'CUSTOMER', 'ACTIVE', NULL, NULL, 'Phnom Penh', 'Key Customer User', t - INTERVAL '5 days', t, 1);

    INSERT INTO user_roles (user_id, role_id) SELECT business_user_id, role_business UNION ALL SELECT customer_user_id, role_customer;

    -- ========== OTHER 2000 PLATFORM ADMINS ==========
    INSERT INTO users (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, user_identifier, email, password, first_name, last_name, phone_number, profile_image_url, user_type, account_status, business_id, position, address, notes, last_login_at, last_active_at, active_sessions_count)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, 'admin' || n || '@platform.com', 'admin' || n || '@platform.com', '$2a$12$hgZ6m7pwOA8AYv.r7YbuN.Yi8gHh.5NWqpEd2Jn6sgCRyu29a1DEK', 'Admin' || n, 'User' || n, '+855 10 100 ' || LPAD((n % 10000)::TEXT, 4, '0'), CASE WHEN n % 2 = 0 THEN photo1 ELSE photo2 END, 'PLATFORM_USER', 'ACTIVE', NULL, 'Platform Admin', 'Phnom Penh', 'Platform Admin ' || n, t - (RANDOM() * INTERVAL '90 days'), t - (RANDOM() * INTERVAL '7 days'), 1
    FROM GENERATE_SERIES(1, 2000) n;

    INSERT INTO user_roles (user_id, role_id) SELECT u.id, role_admin FROM users u WHERE u.user_type = 'PLATFORM_USER' AND u.id != platform_user_id;

    -- ========== OTHER 99 BUSINESSES ==========
    INSERT INTO businesses (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, owner_id, name, email, phone, address, description, status, is_subscription_active)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, (SELECT u.id FROM users u WHERE u.user_type = 'PLATFORM_USER' ORDER BY RANDOM() LIMIT 1), 'Business ' || n, 'business' || n || '@test.com', '+855 23 ' || LPAD((n % 10000)::TEXT, 4, '0'), 'Address ' || n || ', Phnom Penh', 'Business description ' || n, 'ACTIVE', CASE WHEN n % 2 = 0 THEN true ELSE false END
    FROM GENERATE_SERIES(1, 99) n;

    -- ========== BUSINESS SETTINGS FOR OTHER BUSINESSES (key_business already has settings) ==========
    INSERT INTO business_settings (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, logo_url, banner_url, business_type, opening_time, closing_time, is_open_24_hours, working_days, timezone, currency, language, usd_to_khr_rate, contact_email, contact_phone, whatsapp_number, facebook_url, instagram_url, website_url, primary_color, secondary_color, email_notifications_enabled, sms_notifications_enabled, order_notifications_enabled, tax_rate, service_charge_percentage, min_order_amount, delivery_radius_km, estimated_delivery_time, terms_and_conditions, privacy_policy, refund_policy)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bs.id, photo1, photo2, 'RESTAURANT', '06:00', '23:00', false, 'MONDAY-SUNDAY', 'Asia/Phnom_Penh', 'USD', 'en', 4100.0, bs.email, bs.phone, '+855 10 100 0001', photo1, photo2, photo1, '#FF6B6B', '#FFE66D', true, false, true, 0.0, 10.0, 5.0, 25.0, '30-45 minutes', 'Fresh guarantee', 'Data protection', 'Full refund'
    FROM businesses bs WHERE bs.id != key_business_id;

    -- ========== SUBSCRIPTIONS FOR ALL BUSINESSES ==========
    INSERT INTO subscriptions (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, plan_id, start_date, end_date, auto_renew)
    SELECT gen_random_uuid(), 0, t - INTERVAL '6 months', t, 'system', 'system', false, NULL, NULL, bs.id, CASE (ROW_NUMBER() OVER (ORDER BY bs.id) % 3) WHEN 0 THEN plan1 WHEN 1 THEN plan2 ELSE plan3 END, t - INTERVAL '6 months', t + INTERVAL '6 months', true
    FROM businesses bs;

    -- ========== BUSINESS ROLES (12 per business) ==========
    INSERT INTO roles (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, name, description, business_id, user_type)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, CASE r WHEN 1 THEN 'Manager' WHEN 2 THEN 'Chef' WHEN 3 THEN 'Sous Chef' WHEN 4 THEN 'Waiter' WHEN 5 THEN 'Cashier' WHEN 6 THEN 'Delivery Driver' WHEN 7 THEN 'Kitchen Staff' WHEN 8 THEN 'Supervisor' WHEN 9 THEN 'Accountant' WHEN 10 THEN 'Marketing' WHEN 11 THEN 'HR Officer' WHEN 12 THEN 'Customer Service' END, 'Business Role ' || r, br.id, 'BUSINESS_USER'
    FROM businesses br, GENERATE_SERIES(1, 12) r;

    -- ========== OTHER 3000 BUSINESS USERS ==========
    INSERT INTO users (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, user_identifier, email, password, first_name, last_name, phone_number, profile_image_url, user_type, account_status, business_id, position, address, notes, last_login_at, last_active_at, active_sessions_count)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, 'staff' || b_idx || '-' || s || '@business.com', 'staff' || b_idx || '-' || s || '@business.com', '$2a$12$hgZ6m7pwOA8AYv.r7YbuN.Yi8gHh.5NWqpEd2Jn6sgCRyu29a1DEK', 'Staff', 'Member ' || b_idx || '-' || s, '+855 10 200 ' || LPAD((s)::TEXT, 4, '0'), CASE WHEN s % 2 = 0 THEN photo1 ELSE photo2 END, 'BUSINESS_USER', 'ACTIVE', (SELECT id FROM businesses WHERE id != key_business_id ORDER BY id OFFSET ((b_idx - 1) % 99) LIMIT 1), 'Position ' || s, 'Business Address', 'Staff ' || b_idx || '-' || s, t - (RANDOM() * INTERVAL '30 days'), t - (RANDOM() * INTERVAL '2 days'), 1
    FROM GENERATE_SERIES(1, 100) b_idx, GENERATE_SERIES(1, 30) s;

    INSERT INTO user_roles (user_id, role_id) SELECT u.id, r.id FROM users u JOIN businesses b ON u.business_id = b.id JOIN roles r ON r.business_id = b.id WHERE u.user_type = 'BUSINESS_USER' AND u.id != business_user_id LIMIT 3000;

    -- ========== OTHER 6000 CUSTOMERS ==========
    INSERT INTO users (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, user_identifier, email, password, first_name, last_name, phone_number, profile_image_url, user_type, account_status, business_id, position, address, notes, last_login_at, last_active_at, active_sessions_count)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, 'customer' || n || '@test.com', 'customer' || n || '@test.com', '$2a$12$hgZ6m7pwOA8AYv.r7YbuN.Yi8gHh.5NWqpEd2Jn6sgCRyu29a1DEK', 'Customer' || n, 'User' || n, '+855 10 300 ' || LPAD((n % 10000)::TEXT, 4, '0'), CASE WHEN n % 2 = 0 THEN photo1 ELSE photo2 END, 'CUSTOMER', 'ACTIVE', NULL, NULL, 'Phnom Penh', 'Customer ' || n, t - (RANDOM() * INTERVAL '60 days'), t - (RANDOM() * INTERVAL '5 days'), 1
    FROM GENERATE_SERIES(1, 6000) n;

    INSERT INTO user_roles (user_id, role_id) SELECT u.id, role_customer FROM users u WHERE u.user_type = 'CUSTOMER';

    -- ========== CATEGORIES & BRANDS FOR ALL BUSINESSES ==========
    INSERT INTO categories (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, name, image_url, status)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, b.id, 'Category ' || c, CASE WHEN c % 2 = 0 THEN photo1 ELSE photo2 END, 'ACTIVE'
    FROM businesses b, GENERATE_SERIES(1, 100) c;

    INSERT INTO brands (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, name, image_url, description, status)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, b.id, 'Brand ' || br, CASE WHEN br % 2 = 0 THEN photo1 ELSE photo2 END, 'Brand description ' || br, 'ACTIVE'
    FROM businesses b, GENERATE_SERIES(1, 100) br;

    -- ========== PRODUCTS & IMAGES ==========
    INSERT INTO products (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, category_id, brand_id, name, description, status, price, promotion_type, promotion_value, promotion_from_date, promotion_to_date, display_price, display_origin_price, display_promotion_type, display_promotion_value, display_promotion_from_date, display_promotion_to_date, has_sizes, has_active_promotion, view_count, favorite_count, main_image_url)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, b.id, (SELECT id FROM categories WHERE business_id = b.id ORDER BY RANDOM() LIMIT 1), (SELECT id FROM brands WHERE business_id = b.id ORDER BY RANDOM() LIMIT 1), 'Product ' || p, 'Product description ' || p, 'ACTIVE', (15.00 + (p % 80))::NUMERIC, CASE WHEN p % 3 = 0 THEN 'PERCENTAGE' ELSE NULL END, CASE WHEN p % 3 = 0 THEN 10 ELSE NULL END, CASE WHEN p % 3 = 0 THEN t - INTERVAL '5 days' ELSE NULL END, CASE WHEN p % 3 = 0 THEN t + INTERVAL '30 days' ELSE NULL END, (15.00 + (p % 80))::NUMERIC, (15.00 + (p % 80))::NUMERIC, CASE WHEN p % 3 = 0 THEN 'PERCENTAGE' ELSE NULL END, CASE WHEN p % 3 = 0 THEN 10 ELSE NULL END, CASE WHEN p % 3 = 0 THEN t - INTERVAL '5 days' ELSE NULL END, CASE WHEN p % 3 = 0 THEN t + INTERVAL '30 days' ELSE NULL END, CASE WHEN p % 5 = 0 THEN true ELSE false END, CASE WHEN p % 3 = 0 THEN true ELSE false END, (p % 500), (p % 100), CASE WHEN p % 2 = 0 THEN photo1 ELSE photo2 END
    FROM businesses b, GENERATE_SERIES(1, 100) p;

    INSERT INTO product_images (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, product_id, image_url)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, p.id, CASE WHEN (ROW_NUMBER() OVER (PARTITION BY p.id) - 1) % 2 = 0 THEN photo1 ELSE photo2 END
    FROM products p, GENERATE_SERIES(1, 4) img_num;

    INSERT INTO product_sizes (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, product_id, name, price, promotion_type, promotion_value, promotion_from_date, promotion_to_date)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, p.id, CASE s WHEN 1 THEN 'Small' WHEN 2 THEN 'Medium' WHEN 3 THEN 'Large' WHEN 4 THEN 'Extra Large' END, (p.price + (s * 2))::NUMERIC, p.promotion_type, p.promotion_value, p.promotion_from_date, p.promotion_to_date
    FROM (SELECT id, price, promotion_type, promotion_value, promotion_from_date, promotion_to_date FROM products WHERE has_sizes = true) p, GENERATE_SERIES(1, 4) s;

    -- ========== ADDRESSES FOR ALL CUSTOMERS (LOTS FOR KEY CUSTOMER) ==========
    -- 100+ addresses for key customer
    INSERT INTO customer_addresses (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, user_id, village, commune, district, province, country, street_number, house_number, note, latitude, longitude, is_default)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, customer_user_id, 'Village ' || a, 'Commune ' || a, 'District ' || a, 'Phnom Penh', 'Cambodia', 'Street ' || a, 'House ' || a, 'Apt ' || a, 11.5564 + (a::NUMERIC / 1000), 104.9282 + (a::NUMERIC / 1000), a = 1
    FROM GENERATE_SERIES(1, 150) a;

    -- Other customers: normal addresses
    INSERT INTO customer_addresses (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, user_id, village, commune, district, province, country, street_number, house_number, note, latitude, longitude, is_default)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, (SELECT id FROM users WHERE user_type = 'CUSTOMER' AND id != customer_user_id ORDER BY RANDOM() LIMIT 1), 'Village ' || a, 'Commune ' || a, 'District ' || a, 'Phnom Penh', 'Cambodia', 'Street ' || a, 'House ' || a, 'Apt ' || a, 11.5564 + (a::NUMERIC / 1000), 104.9282 + (a::NUMERIC / 1000), a % 3 = 1
    FROM GENERATE_SERIES(1, 14850) a;

    -- ========== DELIVERY OPTIONS FOR ALL BUSINESSES ==========
    INSERT INTO delivery_options (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, name, description, image_url, price, status)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, b.id, CASE d WHEN 1 THEN 'Standard Delivery' WHEN 2 THEN 'Express Delivery' WHEN 3 THEN 'Scheduled Delivery' WHEN 4 THEN 'Pickup' WHEN 5 THEN 'Dine-in' END, CASE d WHEN 1 THEN 'Regular delivery' WHEN 2 THEN 'Fast delivery' WHEN 3 THEN 'Scheduled' WHEN 4 THEN 'Pickup' WHEN 5 THEN 'Dine in' END, CASE WHEN d % 2 = 0 THEN photo1 ELSE photo2 END, CASE d WHEN 1 THEN 2.00 WHEN 2 THEN 4.00 WHEN 3 THEN 2.50 WHEN 4 THEN 0.00 WHEN 5 THEN 0.00 END, 'ACTIVE'
    FROM businesses b, GENERATE_SERIES(1, 5) d;

    -- ========== BANNERS FOR ALL BUSINESSES ==========
    INSERT INTO banners (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, image_url, link_url, status)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, b.id, CASE WHEN bn % 2 = 0 THEN photo1 ELSE photo2 END, '/menu', 'ACTIVE'
    FROM businesses b, GENERATE_SERIES(1, 10) bn;

    -- ========== CARTS & FAVORITES ==========
    INSERT INTO carts (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, user_id, business_id)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, u.id, (SELECT id FROM businesses ORDER BY RANDOM() LIMIT 1)
    FROM (SELECT id FROM users WHERE user_type = 'CUSTOMER' ORDER BY RANDOM() LIMIT 6000) u;

    -- LOTS of cart items for key customer
    INSERT INTO cart_items (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, cart_id, product_id, product_size_id, quantity)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, (SELECT id FROM carts WHERE user_id = customer_user_id LIMIT 1), (SELECT id FROM products ORDER BY RANDOM() LIMIT 1), NULL, (1 + (ci % 5))
    FROM GENERATE_SERIES(1, 500) ci;

    -- Other cart items
    INSERT INTO cart_items (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, cart_id, product_id, product_size_id, quantity)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, (SELECT id FROM carts WHERE user_id != customer_user_id ORDER BY RANDOM() LIMIT 1), (SELECT id FROM products ORDER BY RANDOM() LIMIT 1), NULL, (1 + (ci % 5))
    FROM GENERATE_SERIES(1, 29500) ci;

    INSERT INTO product_favorites (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, user_id, product_id)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, (SELECT id FROM users WHERE user_type = 'CUSTOMER' ORDER BY RANDOM() LIMIT 1), (SELECT id FROM products ORDER BY RANDOM() LIMIT 1)
    FROM GENERATE_SERIES(1, 10000) pf
    ON CONFLICT DO NOTHING;

    -- ========== ORDER PROCESS STATUSES ==========
    INSERT INTO order_process_statuses (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, name, description, status)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, b.id, CASE s WHEN 1 THEN 'Pending' WHEN 2 THEN 'Confirmed' WHEN 3 THEN 'Preparing' WHEN 4 THEN 'Ready' WHEN 5 THEN 'In Delivery' WHEN 6 THEN 'Delivered' WHEN 7 THEN 'Completed' WHEN 8 THEN 'Cancelled' WHEN 9 THEN 'Refunded' WHEN 10 THEN 'Failed' END, CASE s WHEN 1 THEN 'Order received' WHEN 2 THEN 'Order confirmed' WHEN 3 THEN 'Kitchen preparing' WHEN 4 THEN 'Ready for delivery' WHEN 5 THEN 'Out for delivery' WHEN 6 THEN 'Successfully delivered' WHEN 7 THEN 'Order completed' WHEN 8 THEN 'Order cancelled' WHEN 9 THEN 'Order refunded' WHEN 10 THEN 'Delivery failed' END, 'ACTIVE'
    FROM businesses b, GENERATE_SERIES(1, 10) s;

    INSERT INTO reference_counters (entity_type, counter_date, counter_value) VALUES ('ORDER', CURRENT_DATE, 0) ON CONFLICT (entity_type, counter_date) DO NOTHING;

    -- ========== ORDERS (LOTS FOR KEY CUSTOMER) ==========
    -- 1000 orders for key customer
    INSERT INTO orders (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, customer_id, order_number, order_process_status_name, delivery_address_snapshot, delivery_option_snapshot, subtotal, discount_amount, delivery_fee, tax_amount, total_amount, payment_method, payment_status, customer_note, business_note, confirmed_at, completed_at)
    SELECT gen_random_uuid(), 0, t - (RANDOM() * INTERVAL '120 days'), t, 'system', 'system', false, NULL, NULL, key_business_id, customer_user_id, 'ORD-' || TO_CHAR(NOW(), 'YYYYMMDD') || '-0' || LPAD(ord_n::TEXT, 5, '0'), CASE WHEN ord_n % 4 = 0 THEN 'Pending' WHEN ord_n % 4 = 1 THEN 'Confirmed' WHEN ord_n % 4 = 2 THEN 'Preparing' ELSE 'Delivered' END, '{"village":"Village","commune":"Commune","district":"District","province":"Phnom Penh","street":"Street","house":"House"}', '{"name":"Standard","price":2.00}', (20.00 + (ord_n % 100))::NUMERIC, (ord_n % 5)::NUMERIC, (2.00 + (ord_n % 8))::NUMERIC, (ord_n % 3)::NUMERIC, ((20.00 + (ord_n % 100)) - (ord_n % 5) + (2.00 + (ord_n % 8)) + (ord_n % 3))::NUMERIC, CASE (ord_n % 4) WHEN 0 THEN 'CASH' WHEN 1 THEN 'BANK_TRANSFER' ELSE 'ONLINE' END, CASE (ord_n % 3) WHEN 0 THEN 'UNPAID' WHEN 1 THEN 'PAID' ELSE 'COMPLETED' END, 'Note ' || ord_n, 'Business note ' || ord_n, CASE WHEN ord_n % 2 = 0 THEN t - (RANDOM() * INTERVAL '100 days') ELSE NULL END, CASE WHEN ord_n % 3 = 2 THEN t - (RANDOM() * INTERVAL '60 days') ELSE NULL END
    FROM GENERATE_SERIES(1, 1000) ord_n;

    -- Other orders (19000) with different prefix to avoid duplicates
    INSERT INTO orders (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, customer_id, order_number, order_process_status_name, delivery_address_snapshot, delivery_option_snapshot, subtotal, discount_amount, delivery_fee, tax_amount, total_amount, payment_method, payment_status, customer_note, business_note, confirmed_at, completed_at)
    SELECT gen_random_uuid(), 0, t - (RANDOM() * INTERVAL '120 days'), t, 'system', 'system', false, NULL, NULL, (SELECT id FROM businesses ORDER BY RANDOM() LIMIT 1), (SELECT id FROM users WHERE user_type = 'CUSTOMER' AND id != customer_user_id ORDER BY RANDOM() LIMIT 1), 'ORD-' || TO_CHAR(NOW(), 'YYYYMMDD') || '-1' || LPAD((1000 + ord_n)::TEXT, 5, '0'), 'Pending', '{"village":"Village","commune":"Commune","district":"District","province":"Phnom Penh","street":"Street","house":"House"}', '{"name":"Standard","price":2.00}', (20.00 + (ord_n % 100))::NUMERIC, (ord_n % 5)::NUMERIC, (2.00 + (ord_n % 8))::NUMERIC, (ord_n % 3)::NUMERIC, ((20.00 + (ord_n % 100)) - (ord_n % 5) + (2.00 + (ord_n % 8)) + (ord_n % 3))::NUMERIC, CASE (ord_n % 4) WHEN 0 THEN 'CASH' WHEN 1 THEN 'BANK_TRANSFER' ELSE 'ONLINE' END, CASE (ord_n % 3) WHEN 0 THEN 'UNPAID' WHEN 1 THEN 'PAID' ELSE 'COMPLETED' END, 'Note ' || ord_n, 'Business note ' || ord_n, CASE WHEN ord_n % 2 = 0 THEN t - (RANDOM() * INTERVAL '100 days') ELSE NULL END, CASE WHEN ord_n % 3 = 2 THEN t - (RANDOM() * INTERVAL '60 days') ELSE NULL END
    FROM GENERATE_SERIES(1, 19000) ord_n;

    INSERT INTO order_items (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, order_id, product_id, product_size_id, product_name, product_image_url, size_name, current_price, final_price, unit_price, quantity, total_price, has_promotion)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, (SELECT id FROM orders ORDER BY RANDOM() LIMIT 1), (SELECT id FROM products ORDER BY RANDOM() LIMIT 1), NULL, 'Item', CASE WHEN oi_n % 2 = 0 THEN photo1 ELSE photo2 END, CASE WHEN oi_n % 2 = 0 THEN 'Medium' ELSE 'Large' END, (10.00 + (oi_n % 50))::NUMERIC(10,2), (10.00 + (oi_n % 50))::NUMERIC(10,2), (10.00 + (oi_n % 50))::NUMERIC(10,2), (1 + (oi_n % 5))::INTEGER, ((10.00 + (oi_n % 50)) * (1 + (oi_n % 5)))::NUMERIC(10,2), false
    FROM GENERATE_SERIES(1, 60000) oi_n;

    INSERT INTO order_status_history (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, order_id, order_process_status_id, note, changed_by_user_id)
    SELECT gen_random_uuid(), 0, t - (RANDOM() * INTERVAL '120 days'), t, 'system', 'system', false, NULL, NULL, osh_ord.id, osh_stat.id, 'Created', NULL
    FROM (SELECT id FROM orders ORDER BY RANDOM()) osh_ord, (SELECT id FROM order_process_statuses ORDER BY RANDOM() LIMIT 1) osh_stat;

    INSERT INTO order_payments (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, order_id, payment_reference, subtotal, discount_amount, delivery_fee, tax_amount, total_amount, payment_method, status, customer_payment_method)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, opay_b.id, opay_ord.id, 'PAY-' || LPAD(opay_n::TEXT, 8, '0'), (20.00 + (opay_n % 100))::NUMERIC, (opay_n % 5)::NUMERIC, (2.00 + (opay_n % 8))::NUMERIC, (opay_n % 3)::NUMERIC, ((20.00 + (opay_n % 100)) - (opay_n % 5) + (2.00 + (opay_n % 8)) + (opay_n % 3))::NUMERIC, CASE (opay_n % 4) WHEN 0 THEN 'CASH' WHEN 1 THEN 'BANK_TRANSFER' ELSE 'ONLINE' END, CASE (opay_n % 3) WHEN 0 THEN 'PENDING' WHEN 1 THEN 'FAILED' ELSE 'COMPLETED' END, 'Cash'
    FROM (SELECT id FROM businesses ORDER BY RANDOM() LIMIT 1) opay_b, (SELECT id FROM orders ORDER BY RANDOM() LIMIT 1) opay_ord, GENERATE_SERIES(1, 20000) opay_n;

    -- ========== WORK SCHEDULES (LOTS FOR KEY BUSINESS USER) ==========
    -- 500 schedules for key business user
    INSERT INTO work_schedules (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, user_id, name, schedule_type_enum, start_time, end_time, break_start_time, break_end_time)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, key_business_id, business_user_id, 'Schedule ' || ws, CASE WHEN ws % 2 = 0 THEN 'MORNING_SHIFT' ELSE 'EVENING_SHIFT' END, CASE WHEN ws % 3 = 0 THEN '06:00'::TIME WHEN ws % 3 = 1 THEN '10:00'::TIME ELSE '14:00'::TIME END, CASE WHEN ws % 3 = 0 THEN '14:00'::TIME WHEN ws % 3 = 1 THEN '18:00'::TIME ELSE '22:00'::TIME END, '12:00'::TIME, '13:00'::TIME
    FROM GENERATE_SERIES(1, 500) ws;

    -- Other work schedules
    INSERT INTO work_schedules (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, user_id, name, schedule_type_enum, start_time, end_time, break_start_time, break_end_time)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, ws_u.business_id, ws_u.id, 'Schedule ' || ws, CASE WHEN ws % 2 = 0 THEN 'MORNING_SHIFT' ELSE 'EVENING_SHIFT' END, CASE WHEN ws % 3 = 0 THEN '06:00'::TIME WHEN ws % 3 = 1 THEN '10:00'::TIME ELSE '14:00'::TIME END, CASE WHEN ws % 3 = 0 THEN '14:00'::TIME WHEN ws % 3 = 1 THEN '18:00'::TIME ELSE '22:00'::TIME END, '12:00'::TIME, '13:00'::TIME
    FROM (SELECT id, business_id FROM users WHERE user_type = 'BUSINESS_USER' AND id != business_user_id) ws_u, GENERATE_SERIES(1, 10) ws;

    -- ========== ATTENDANCES (LOTS FOR KEY BUSINESS USER) ==========
    -- 500 attendance records for key business user
    INSERT INTO attendances (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, reference_number, user_id, business_id, work_schedule_id, attendance_date, status, remarks)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, 'ATT-' || gen_random_uuid()::TEXT, business_user_id, key_business_id, (SELECT id FROM work_schedules WHERE user_id = business_user_id LIMIT 1), (t - (INTERVAL '1 day' * (att % 60)))::DATE, CASE WHEN att % 10 = 0 THEN 'ABSENT' ELSE 'PRESENT' END, 'Attendance record ' || att
    FROM GENERATE_SERIES(1, 500) att;

    -- Other attendance records
    INSERT INTO attendances (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, reference_number, user_id, business_id, work_schedule_id, attendance_date, status, remarks)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, 'ATT-' || gen_random_uuid()::TEXT, att_u.id, att_u.business_id, (SELECT id FROM work_schedules WHERE user_id = att_u.id LIMIT 1), (t - (INTERVAL '1 day' * (att % 60)))::DATE, CASE WHEN att % 10 = 0 THEN 'ABSENT' ELSE 'PRESENT' END, 'Attendance record ' || att
    FROM (SELECT id, business_id FROM users WHERE user_type = 'BUSINESS_USER' AND id != business_user_id) att_u, GENERATE_SERIES(1, 5) att;

    -- ========== ATTENDANCE CHECK-INS (LOTS FOR KEY BUSINESS USER) ==========
    -- 1000 check-ins for key business user
    INSERT INTO attendance_check_ins (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, reference_number, attendance_id, check_in_type, check_in_time, latitude, longitude, remarks)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, 'CHK-' || gen_random_uuid()::TEXT, (SELECT id FROM attendances WHERE user_id = business_user_id ORDER BY RANDOM() LIMIT 1), CASE WHEN aci % 2 = 0 THEN 'START' ELSE 'END' END, (t - (INTERVAL '1 day' * (aci % 60)))::TIMESTAMP + ('06:' || LPAD((aci % 60)::TEXT, 2, '0') || ':00')::TIME, 11.5564 + (aci::NUMERIC / 1000), 104.9282 + (aci::NUMERIC / 1000), 'Check-in ' || aci
    FROM GENERATE_SERIES(1, 1000) aci;

    -- Other check-ins
    INSERT INTO attendance_check_ins (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, reference_number, attendance_id, check_in_type, check_in_time, latitude, longitude, remarks)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, 'CHK-' || gen_random_uuid()::TEXT, (SELECT id FROM attendances WHERE user_id != business_user_id ORDER BY RANDOM() LIMIT 1), CASE WHEN aci % 2 = 0 THEN 'START' ELSE 'END' END, (t - (INTERVAL '1 day' * (aci % 60)))::TIMESTAMP + ('06:' || LPAD((aci % 60)::TEXT, 2, '0') || ':00')::TIME, 11.5564 + (aci::NUMERIC / 1000), 104.9282 + (aci::NUMERIC / 1000), 'Check-in ' || aci
    FROM GENERATE_SERIES(1, 9000) aci;

    -- ========== LEAVES (LOTS FOR KEY BUSINESS USER) ==========
    -- 100 leave records for key business user
    INSERT INTO leaves (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, reference_number, user_id, business_id, leave_type_enum, start_date, end_date, total_days, reason, status, action_by, action_at, action_note)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, 'LEV-' || gen_random_uuid()::TEXT, business_user_id, key_business_id, (SELECT id FROM leave_type_enum ORDER BY RANDOM() LIMIT 1), (t + (INTERVAL '1 day' * (lv % 365)))::DATE, (t + (INTERVAL '1 day' * ((lv % 365) + 3)))::DATE, (3 + (lv % 5))::DOUBLE PRECISION, 'Leave reason ' || lv, CASE WHEN lv % 4 = 0 THEN 'PENDING' WHEN lv % 4 = 1 THEN 'APPROVED' WHEN lv % 4 = 2 THEN 'REJECTED' ELSE 'CANCELLED' END, CASE WHEN lv % 2 = 0 THEN business_user_id ELSE NULL END, CASE WHEN lv % 2 = 0 THEN t ELSE NULL END, CASE WHEN lv % 2 = 0 THEN 'Approved' ELSE NULL END
    FROM GENERATE_SERIES(1, 100) lv;

    -- Other leave records
    INSERT INTO leaves (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, reference_number, user_id, business_id, leave_type_enum, start_date, end_date, total_days, reason, status, action_by, action_at, action_note)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, 'LEV-' || gen_random_uuid()::TEXT, lv_u.id, lv_u.business_id, (SELECT id FROM leave_type_enum ORDER BY RANDOM() LIMIT 1), (t + (INTERVAL '1 day' * (lv % 365)))::DATE, (t + (INTERVAL '1 day' * ((lv % 365) + 3)))::DATE, (3 + (lv % 5))::DOUBLE PRECISION, 'Leave reason ' || lv, CASE WHEN lv % 4 = 0 THEN 'PENDING' WHEN lv % 4 = 1 THEN 'APPROVED' WHEN lv % 4 = 2 THEN 'REJECTED' ELSE 'CANCELLED' END, CASE WHEN lv % 2 = 0 THEN (SELECT id FROM users lv_uu WHERE lv_uu.business_id = lv_u.business_id AND lv_uu.user_type = 'BUSINESS_USER' ORDER BY RANDOM() LIMIT 1) ELSE NULL END, CASE WHEN lv % 2 = 0 THEN t ELSE NULL END, CASE WHEN lv % 2 = 0 THEN 'Approved' ELSE NULL END
    FROM (SELECT id, business_id FROM users WHERE user_type = 'BUSINESS_USER' AND id != business_user_id) lv_u, GENERATE_SERIES(1, 2) lv;

    -- ========== EXCHANGE RATES ==========
    INSERT INTO exchange_rates (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, usd_to_khr_rate, is_active, notes)
    VALUES (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, 4100.0, true, 'Standard rate');

    INSERT INTO business_exchange_rates (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, usd_to_khr_rate, usd_to_thb_rate, usd_to_cny_rate, usd_to_vnd_rate, is_active, notes)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, ber_b.id, 4105.0 + (ber % 10)::NUMERIC, 35.45 + (ber % 1)::NUMERIC, 7.25 + (ber % 1)::NUMERIC, 24500.0 + (ber % 100)::NUMERIC, true, 'Custom rates ' || ber
    FROM businesses ber_b, GENERATE_SERIES(1, 2) ber;

    RAISE NOTICE '✅ MASSIVE TEST DATA GENERATED WITH 3 KEY USERS!';
    RAISE NOTICE '';
    RAISE NOTICE '🔑 KEY USERS FOR LOGIN:';
    RAISE NOTICE '  1️⃣ Platform: phatmenghor19@gmail.com';
    RAISE NOTICE '  2️⃣ Business: phatmenghor20@gmail.com';
    RAISE NOTICE '  3️⃣ Customer: phatmenghor21@gmail.com';
    RAISE NOTICE '  🔐 Password: (hashed in DB)';
    RAISE NOTICE '';
    RAISE NOTICE '📊 DATA SUMMARY:';
    RAISE NOTICE '✓ Platform User: 1 key + 2000 other = 2001 total';
    RAISE NOTICE '✓ Business Users: 1 key + 3000 other = 3001 total';
    RAISE NOTICE '✓ Customers: 1 key + 6000 other = 6001 total';
    RAISE NOTICE '✓ Businesses: 1 key + 99 other = 100 total';
    RAISE NOTICE '';
    RAISE NOTICE '🎯 KEY USER DATA:';
    RAISE NOTICE '  • Key Platform User: All 2001 Platform Admins access';
    RAISE NOTICE '  • Key Business User: 500 work schedules, 500 attendances, 1000 check-ins, 100 leaves';
    RAISE NOTICE '  • Key Customer: 150 addresses, 1000 orders, 500 cart items';
    RAISE NOTICE '✓ 100 Businesses | ✓ 10,000 Products | ✓ 40,000 Product Images';
    RAISE NOTICE '✓ 20,000 Orders | ✓ 60,000 Order Items';
    RAISE NOTICE '✓ 6000 Carts | ✓ 30,000 Cart Items | ✓ 10,000 Favorites';
    RAISE NOTICE '✓ 15,000 Customer Addresses | ✓ 5,000+ Work Schedules';
    RAISE NOTICE '✓ 15,500+ Attendance Records | ✓ 10,000+ Check-ins';
    RAISE NOTICE '✓ 2,100+ Leave Records | ✓ Exchange Rates';

END $$;

SELECT 'DONE ✅ ALL TEST DATA POPULATED!' as status;
