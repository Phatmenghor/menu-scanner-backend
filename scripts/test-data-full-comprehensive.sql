-- ============================================================
-- COMPLETE TEST DATA - ALL TABLES FULLY POPULATED
-- ============================================================
-- ONLY 2 Unsplash Premium Photos
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
    plan1 UUID := gen_random_uuid();
BEGIN

    -- ========== CLEANUP ==========
    DELETE FROM user_roles WHERE user_id IN (SELECT id FROM users WHERE user_identifier IN ('phatmenghor19@gmail.com','phatmenghor20@gmail.com','phatmenghor21@gmail.com'));
    DELETE FROM users WHERE user_identifier IN ('phatmenghor19@gmail.com','phatmenghor20@gmail.com','phatmenghor21@gmail.com');
    DELETE FROM roles WHERE name IN ('PLATFORM_ADMIN', 'BUSINESS_OWNER', 'CUSTOMER', 'STAFF');

    -- ========== ROLES ==========
    INSERT INTO roles (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, name, description, business_id, user_type)
    VALUES
        (role_admin, 0, t, t, 'system', 'system', false, NULL, NULL, 'PLATFORM_ADMIN', 'Platform Admin', NULL, 'PLATFORM_USER'),
        (role_business, 0, t, t, 'system', 'system', false, NULL, NULL, 'BUSINESS_OWNER', 'Business Owner', NULL, 'BUSINESS_USER'),
        (role_customer, 0, t, t, 'system', 'system', false, NULL, NULL, 'CUSTOMER', 'Customer', NULL, 'CUSTOMER'),
        (role_staff, 0, t, t, 'system', 'system', false, NULL, NULL, 'STAFF', 'Staff', NULL, 'BUSINESS_USER');

    -- ========== 3 MAIN USERS ==========
    INSERT INTO users (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, user_identifier, email, password, first_name, last_name, phone_number, profile_image_url, user_type, account_status, business_id, position, address, notes, last_login_at, last_active_at, active_sessions_count)
    VALUES
        (puid, 0, t, t, 'system', 'system', false, NULL, NULL, 'phatmenghor19@gmail.com', 'phatmenghor19@gmail.com', '$2a$12$hgZ6m7pwOA8AYv.r7YbuN.Yi8gHh.5NWqpEd2Jn6sgCRyu29a1DEK', 'Phat', 'Menghor', '+855 10 123 4567', photo1, 'PLATFORM_USER', 'ACTIVE', NULL, 'Admin', 'Phnom Penh', 'Platform Admin', t - INTERVAL '2 hours', t - INTERVAL '1 hour', 1),
        (buid, 0, t, t, 'system', 'system', false, NULL, NULL, 'phatmenghor20@gmail.com', 'phatmenghor20@gmail.com', '$2a$12$hgZ6m7pwOA8AYv.r7YbuN.Yi8gHh.5NWqpEd2Jn6sgCRyu29a1DEK', 'Menghor', 'Business', '+855 10 234 5678', photo2, 'BUSINESS_USER', 'ACTIVE', NULL, 'Owner', 'Street 252', 'Business Owner', t - INTERVAL '1 hour', t - INTERVAL '30 minutes', 2),
        (cuid, 0, t, t, 'system', 'system', false, NULL, NULL, 'phatmenghor21@gmail.com', 'phatmenghor21@gmail.com', '$2a$12$hgZ6m7pwOA8AYv.r7YbuN.Yi8gHh.5NWqpEd2Jn6sgCRyu29a1DEK', 'Customer', 'Menghor', '+855 10 345 6789', photo1, 'CUSTOMER', 'ACTIVE', NULL, NULL, 'Phnom Penh', 'Customer', t - INTERVAL '3 hours', t - INTERVAL '45 minutes', 1);

    INSERT INTO user_roles (user_id, role_id) VALUES (puid, role_admin), (buid, role_business), (cuid, role_customer);

    -- ========== BUSINESS ==========
    INSERT INTO businesses (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, owner_id, name, email, phone, address, description, status, is_subscription_active)
    VALUES (bid, 0, t, t, 'system', 'system', false, NULL, NULL, buid, 'Phat Restaurant', 'phatmenghor20@gmail.com', '+855 23 999 888', 'Street 252, Phnom Penh', 'Best Khmer restaurant', 'ACTIVE', true);

    UPDATE users SET business_id = bid WHERE id = buid;

    -- ========== BUSINESS SETTINGS ==========
    INSERT INTO business_settings (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, logo_url, banner_url, business_type, opening_time, closing_time, is_open_24_hours, working_days, timezone, currency, language, usd_to_khr_rate, contact_email, contact_phone, whatsapp_number, facebook_url, instagram_url, website_url, primary_color, secondary_color, email_notifications_enabled, sms_notifications_enabled, order_notifications_enabled, tax_rate, service_charge_percentage, min_order_amount, delivery_radius_km, estimated_delivery_time, terms_and_conditions, privacy_policy, refund_policy)
    VALUES (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, photo1, photo2, 'RESTAURANT', '06:00', '23:00', false, 'MONDAY-SUNDAY', 'Asia/Phnom_Penh', 'USD', 'en', 4100.0, 'phatmenghor20@gmail.com', '+855 23 999 888', '+855 10 234 5678', photo1, photo2, photo1, '#FF6B6B', '#FFE66D', true, false, true, 0.0, 10.0, 5.0, 25.0, '30-45 minutes', 'Fresh guarantee', 'Data protection', 'Full refund');

    -- ========== SUBSCRIPTION PLAN ==========
    INSERT INTO subscription_plans (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, name, description, price, duration_days, status)
    VALUES (plan1, 0, t, t, 'system', 'system', false, NULL, NULL, 'Annual Premium', 'Full access for 1 year', 299.99, 365, 'PUBLIC');

    -- ========== SUBSCRIPTION ==========
    INSERT INTO subscriptions (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, plan_id, start_date, end_date, auto_renew)
    VALUES (gen_random_uuid(), 0, t - INTERVAL '1 year', t, 'system', 'system', false, NULL, NULL, bid, plan1, t - INTERVAL '1 year', t + INTERVAL '1 year', true);

    -- ========== 12 BUSINESS ROLES ==========
    INSERT INTO roles (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, name, description, business_id, user_type)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, CASE n WHEN 1 THEN 'Manager' WHEN 2 THEN 'Chef' WHEN 3 THEN 'Sous Chef' WHEN 4 THEN 'Waiter' WHEN 5 THEN 'Cashier' WHEN 6 THEN 'Delivery Driver' WHEN 7 THEN 'Kitchen Staff' WHEN 8 THEN 'Supervisor' WHEN 9 THEN 'Accountant' WHEN 10 THEN 'Marketing' WHEN 11 THEN 'HR Officer' WHEN 12 THEN 'Customer Service' END, 'Role ' || n, bid, 'BUSINESS_USER' FROM GENERATE_SERIES(1, 12) n;

    -- ========== 10 CATEGORIES ==========
    INSERT INTO categories (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, name, image_url, status)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, 'Category ' || n, CASE WHEN n % 2 = 0 THEN photo1 ELSE photo2 END, 'ACTIVE' FROM GENERATE_SERIES(1, 10) n;

    -- ========== 10 BRANDS ==========
    INSERT INTO brands (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, name, image_url, description, status)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, 'Brand ' || n, CASE WHEN n % 2 = 0 THEN photo1 ELSE photo2 END, 'Brand description ' || n, 'ACTIVE' FROM GENERATE_SERIES(1, 10) n;

    -- ========== 1000 PRODUCTS ==========
    INSERT INTO products (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, category_id, brand_id, name, description, status, price, promotion_type, promotion_value, promotion_from_date, promotion_to_date, display_price, display_origin_price, display_promotion_type, display_promotion_value, display_promotion_from_date, display_promotion_to_date, has_sizes, has_active_promotion, view_count, favorite_count, main_image_url)
    SELECT
        gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL,
        bid, (SELECT id FROM categories WHERE business_id = bid ORDER BY RANDOM() LIMIT 1), (SELECT id FROM brands WHERE business_id = bid ORDER BY RANDOM() LIMIT 1),
        'Product ' || n, 'Description for product ' || n, 'ACTIVE', (15.00 + (n % 80))::NUMERIC,
        CASE WHEN n % 3 = 0 THEN 'PERCENTAGE' ELSE NULL END, CASE WHEN n % 3 = 0 THEN 10 ELSE NULL END, CASE WHEN n % 3 = 0 THEN t - INTERVAL '5 days' ELSE NULL END, CASE WHEN n % 3 = 0 THEN t + INTERVAL '30 days' ELSE NULL END,
        (15.00 + (n % 80))::NUMERIC, (15.00 + (n % 80))::NUMERIC, CASE WHEN n % 3 = 0 THEN 'PERCENTAGE' ELSE NULL END, CASE WHEN n % 3 = 0 THEN 10 ELSE NULL END, CASE WHEN n % 3 = 0 THEN t - INTERVAL '5 days' ELSE NULL END, CASE WHEN n % 3 = 0 THEN t + INTERVAL '30 days' ELSE NULL END,
        CASE WHEN n % 5 = 0 THEN true ELSE false END, CASE WHEN n % 3 = 0 THEN true ELSE false END, (n % 500), (n % 100), CASE WHEN n % 2 = 0 THEN photo1 ELSE photo2 END
    FROM GENERATE_SERIES(1, 1000) n;

    -- ========== 4000 PRODUCT IMAGES (4 per product) ==========
    INSERT INTO product_images (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, product_id, image_url)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, p.id, CASE WHEN (ROW_NUMBER() OVER (PARTITION BY p.id) - 1) % 2 = 0 THEN photo1 ELSE photo2 END FROM products p, GENERATE_SERIES(1, 4) img_num WHERE p.business_id = bid;

    -- ========== PRODUCT SIZES (for 200 products) ==========
    INSERT INTO product_sizes (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, product_id, name, price, promotion_type, promotion_value, promotion_from_date, promotion_to_date)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, p.id, CASE s WHEN 1 THEN 'Small' WHEN 2 THEN 'Medium' WHEN 3 THEN 'Large' WHEN 4 THEN 'Extra Large' END, (p.price + (s * 2))::NUMERIC, p.promotion_type, p.promotion_value, p.promotion_from_date, p.promotion_to_date
    FROM (SELECT id, price, promotion_type, promotion_value, promotion_from_date, promotion_to_date FROM products WHERE business_id = bid AND has_sizes = true) p, GENERATE_SERIES(1, 4) s;

    -- ========== 500 CUSTOMERS ==========
    INSERT INTO users (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, user_identifier, email, password, first_name, last_name, phone_number, profile_image_url, user_type, account_status, business_id, position, address, notes, last_login_at, last_active_at, active_sessions_count)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, 'customer' || n || '@test.com', 'customer' || n || '@test.com', '$2a$12$hgZ6m7pwOA8AYv.r7YbuN.Yi8gHh.5NWqpEd2Jn6sgCRyu29a1DEK', 'Customer' || n, 'User' || n, '+855 10 234 ' || LPAD((n % 10000)::TEXT, 4, '0'), CASE WHEN n % 2 = 0 THEN photo1 ELSE photo2 END, 'CUSTOMER', 'ACTIVE', NULL, NULL, 'Phnom Penh', 'Customer ' || n, t - (RANDOM() * INTERVAL '14 days'), t - (RANDOM() * INTERVAL '2 days'), 1 FROM GENERATE_SERIES(1, 500) n;

    INSERT INTO user_roles (user_id, role_id) SELECT u.id, role_customer FROM users u WHERE u.email LIKE 'customer%@test.com' AND NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = u.id);

    -- ========== 300 STAFF USERS ==========
    INSERT INTO users (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, user_identifier, email, password, first_name, last_name, phone_number, profile_image_url, user_type, account_status, business_id, position, address, notes, last_login_at, last_active_at, active_sessions_count)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, 'staff' || n || '@phatrestaurant.com', 'staff' || n || '@phatrestaurant.com', '$2a$12$hgZ6m7pwOA8AYv.r7YbuN.Yi8gHh.5NWqpEd2Jn6sgCRyu29a1DEK', 'Staff' || n, 'Member' || n, '+855 10 123 ' || LPAD((n % 10000)::TEXT, 4, '0'), CASE WHEN n % 2 = 0 THEN photo1 ELSE photo2 END, 'BUSINESS_USER', 'ACTIVE', bid, 'Position ' || n, 'Phnom Penh', 'Staff ' || n, t - (RANDOM() * INTERVAL '30 days'), t - (RANDOM() * INTERVAL '1 day'), 1 FROM GENERATE_SERIES(1, 300) n;

    -- ========== 500 CUSTOMER ADDRESSES ==========
    INSERT INTO customer_addresses (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, user_id, village, commune, district, province, country, street_number, house_number, note, latitude, longitude, is_default)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, (SELECT id FROM users WHERE user_type = 'CUSTOMER' ORDER BY RANDOM() LIMIT 1), 'Village ' || n, 'Commune ' || n, 'District ' || n, 'Phnom Penh', 'Cambodia', 'Street ' || n, 'House ' || n, 'Apt ' || n, 11.5564 + (n::NUMERIC / 1000), 104.9282 + (n::NUMERIC / 1000), n % 3 = 1 FROM GENERATE_SERIES(1, 500) n;

    -- ========== 5 DELIVERY OPTIONS ==========
    INSERT INTO delivery_options (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, name, description, image_url, price, status)
    VALUES
        (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, 'Standard Delivery', 'Regular delivery', photo1, 2.00, 'ACTIVE'),
        (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, 'Express Delivery', 'Fast delivery', photo2, 4.00, 'ACTIVE'),
        (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, 'Scheduled Delivery', 'Scheduled', photo1, 2.50, 'ACTIVE'),
        (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, 'Pickup', 'Pickup', photo2, 0.00, 'ACTIVE'),
        (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, 'Dine-in', 'Dine in', photo1, 0.00, 'ACTIVE');

    -- ========== 5 BANNERS ==========
    INSERT INTO banners (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, image_url, link_url, status)
    VALUES
        (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, photo1, '/menu', 'ACTIVE'),
        (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, photo2, '/menu', 'ACTIVE'),
        (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, photo1, '/menu', 'ACTIVE'),
        (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, photo2, '/menu', 'ACTIVE'),
        (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, photo1, '/menu', 'ACTIVE');

    -- ========== 500 CARTS ==========
    INSERT INTO carts (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, user_id, business_id)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, u.id, bid FROM (SELECT id FROM users WHERE user_type = 'CUSTOMER' ORDER BY RANDOM() LIMIT 500) u;

    -- ========== 2000 CART ITEMS ==========
    INSERT INTO cart_items (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, cart_id, product_id, product_size_id, quantity)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, (SELECT id FROM carts WHERE business_id = bid ORDER BY RANDOM() LIMIT 1), (SELECT id FROM products WHERE business_id = bid ORDER BY RANDOM() LIMIT 1), NULL, (1 + (n % 5)) FROM GENERATE_SERIES(1, 2000) n;

    -- ========== 10 ORDER PROCESS STATUSES ==========
    INSERT INTO order_process_statuses (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, name, description, status)
    VALUES
        (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, 'Pending', 'Order received', 'ACTIVE'),
        (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, 'Confirmed', 'Order confirmed', 'ACTIVE'),
        (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, 'Preparing', 'Kitchen preparing', 'ACTIVE'),
        (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, 'Ready', 'Ready for delivery', 'ACTIVE'),
        (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, 'In Delivery', 'Out for delivery', 'ACTIVE'),
        (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, 'Delivered', 'Successfully delivered', 'ACTIVE'),
        (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, 'Completed', 'Order completed', 'ACTIVE'),
        (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, 'Cancelled', 'Order cancelled', 'ACTIVE'),
        (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, 'Refunded', 'Order refunded', 'ACTIVE'),
        (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, 'Failed', 'Delivery failed', 'ACTIVE');

    -- ========== 2000 ORDERS WITH ITEMS ==========
    INSERT INTO reference_counters (entity_type, counter_date, counter_value) VALUES ('ORDER', CURRENT_DATE, 0) ON CONFLICT (entity_type, counter_date) DO NOTHING;

    FOR n IN 1..2000 LOOP
        DECLARE
            order_id UUID := gen_random_uuid();
            customer_id UUID;
            product_id UUID;
            status_id UUID;
            subtotal NUMERIC;
            discount NUMERIC;
            delivery NUMERIC;
            tax NUMERIC;
            total NUMERIC;
            counter BIGINT;
        BEGIN
            SELECT id INTO customer_id FROM users WHERE user_type = 'CUSTOMER' ORDER BY RANDOM() LIMIT 1;
            SELECT id INTO product_id FROM products WHERE business_id = bid ORDER BY RANDOM() LIMIT 1;
            SELECT id INTO status_id FROM order_process_statuses WHERE business_id = bid AND name = 'Pending' LIMIT 1;

            UPDATE reference_counters SET counter_value = counter_value + 1 WHERE entity_type = 'ORDER' AND counter_date = CURRENT_DATE;
            SELECT counter_value INTO counter FROM reference_counters WHERE entity_type = 'ORDER' AND counter_date = CURRENT_DATE;

            subtotal := (20.00 + (n % 100))::NUMERIC;
            discount := (n % 5)::NUMERIC;
            delivery := (2.00 + (n % 8))::NUMERIC;
            tax := (n % 3)::NUMERIC;
            total := subtotal - discount + delivery + tax;

            INSERT INTO orders (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, customer_id, order_number, order_process_status_name, delivery_address_snapshot, delivery_option_snapshot, subtotal, discount_amount, delivery_fee, tax_amount, total_amount, payment_method, payment_status, customer_note, business_note, confirmed_at, completed_at)
            VALUES (order_id, 0, t - (RANDOM() * INTERVAL '60 days'), t, 'system', 'system', false, NULL, NULL, bid, customer_id, 'ORD-' || TO_CHAR(NOW(), 'YYYYMMDD') || '-' || LPAD(counter::TEXT, 6, '0'), 'Pending', '{"village":"Village","commune":"Commune","district":"District","province":"Phnom Penh","street":"Street","house":"House"}', '{"name":"Standard","price":2.00}', subtotal, discount, delivery, tax, total, CASE (n % 4) WHEN 0 THEN 'CASH' WHEN 1 THEN 'BANK_TRANSFER' ELSE 'ONLINE' END, CASE (n % 3) WHEN 0 THEN 'UNPAID' WHEN 1 THEN 'PAID' ELSE 'COMPLETED' END, 'Note ' || n, 'Business note ' || n, CASE WHEN n % 2 = 0 THEN t - (RANDOM() * INTERVAL '50 days') ELSE NULL END, CASE WHEN n % 3 = 2 THEN t - (RANDOM() * INTERVAL '30 days') ELSE NULL END);

            -- Order items (3 per order)
            FOR i IN 1..3 LOOP
                INSERT INTO order_items (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, order_id, product_id, product_size_id, product_name, product_image_url, size_name, current_price, final_price, unit_price, quantity, total_price, has_promotion)
                VALUES (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, order_id, product_id, NULL, 'Item', CASE WHEN i % 2 = 0 THEN photo1 ELSE photo2 END, CASE WHEN i % 2 = 0 THEN 'Medium' ELSE 'Large' END, (10 + i)::NUMERIC, (10 + i)::NUMERIC, (10 + i)::NUMERIC, i, (i * (10 + i))::NUMERIC, n % 3 = 0);
            END LOOP;

            -- Order status history
            INSERT INTO order_status_history (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, order_id, order_process_status_id, note, changed_by_user_id)
            VALUES (gen_random_uuid(), 0, t - (RANDOM() * INTERVAL '60 days'), t, 'system', 'system', false, NULL, NULL, order_id, status_id, 'Created', NULL);

            -- Order payment
            INSERT INTO order_payments (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, order_id, payment_reference, subtotal, discount_amount, delivery_fee, tax_amount, total_amount, payment_method, status, customer_payment_method)
            VALUES (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, order_id, 'PAY-' || LPAD(n::TEXT, 8, '0'), subtotal, discount, delivery, tax, total, CASE (n % 4) WHEN 0 THEN 'CASH' WHEN 1 THEN 'BANK_TRANSFER' ELSE 'ONLINE' END, CASE (n % 3) WHEN 0 THEN 'PENDING' WHEN 1 THEN 'FAILED' ELSE 'COMPLETED' END, 'Cash');
        END;
    END LOOP;

    -- ========== 1000 PRODUCT FAVORITES ==========
    INSERT INTO product_favorites (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, user_id, product_id)
    SELECT gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, (SELECT id FROM users WHERE user_type = 'CUSTOMER' ORDER BY RANDOM() LIMIT 1), (SELECT id FROM products WHERE business_id = bid ORDER BY RANDOM() LIMIT 1) FROM GENERATE_SERIES(1, 1000) n;

    -- ========== EXCHANGE RATES ==========
    INSERT INTO exchange_rates (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, usd_to_khr_rate, is_active, notes)
    VALUES (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, 4100.0, true, 'Standard rate');

    INSERT INTO business_exchange_rates (id, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at, deleted_by, business_id, usd_to_khr_rate, usd_to_thb_rate, usd_to_cny_rate, usd_to_vnd_rate, is_active, notes)
    VALUES
        (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, 4105.0, 35.45, 7.25, 24500.0, true, 'Custom rates'),
        (gen_random_uuid(), 0, t, t, 'system', 'system', false, NULL, NULL, bid, 4110.0, 35.50, 7.30, 24600.0, true, 'Alternative rates');

    RAISE NOTICE 'COMPLETE TEST DATA GENERATED! ✅';
    RAISE NOTICE '✓ 1 Business | ✓ 3 Admin Users | ✓ 500 Customers | ✓ 300 Staff';
    RAISE NOTICE '✓ 1000 Products | ✓ 4000 Product Images | ✓ 10 Categories | ✓ 10 Brands';
    RAISE NOTICE '✓ 2000 Orders | ✓ 6000 Order Items | ✓ 500 Carts | ✓ 2000 Cart Items';
    RAISE NOTICE '✓ 500 Customer Addresses | ✓ 5 Delivery Options | ✓ 5 Banners';
    RAISE NOTICE '✓ 1000 Product Favorites | ✓ 2 Exchange Rates';
    RAISE NOTICE '✓ ONLY 2 Unsplash Premium Photos used throughout!';

END $$;

SELECT 'DONE ✅ ALL TABLES POPULATED!' as status;
