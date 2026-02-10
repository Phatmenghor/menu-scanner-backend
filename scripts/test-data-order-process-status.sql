-- =====================================================================
-- E-Menu Platform: Dynamic Order Process Status + Test Data Script
-- Loops ALL businesses in database (not hardcoded)
-- Run this in pgAdmin for testing
-- =====================================================================

-- =====================================================================
-- STEP 1: Insert default order process statuses for ALL businesses
-- that don't have any statuses yet
-- =====================================================================
DO $$
DECLARE
    v_biz RECORD;
    v_has_statuses BOOLEAN;
BEGIN
    FOR v_biz IN
        SELECT id, name FROM businesses WHERE is_deleted = false ORDER BY name
    LOOP
        -- Check if this business already has process statuses
        SELECT EXISTS(
            SELECT 1 FROM order_process_statuses
            WHERE business_id = v_biz.id AND is_deleted = false
        ) INTO v_has_statuses;

        IF v_has_statuses THEN
            RAISE NOTICE 'Business "%" already has statuses, skipping...', v_biz.name;
            CONTINUE;
        END IF;

        -- Insert 8 default order process statuses for this business
        INSERT INTO order_process_statuses (id, version, business_id, name, description, color, sort_order, is_default, is_final, status_type, status, is_deleted, created_at, updated_at, created_by, updated_by) VALUES
        (gen_random_uuid(), 0, v_biz.id, 'Pending',           'Order received, waiting for confirmation',  '#FFA500', 1, true,  false, 'ACTIVE',    'ACTIVE', false, NOW(), NOW(), 'system', 'system'),
        (gen_random_uuid(), 0, v_biz.id, 'Confirmed',         'Order accepted by business',                '#2196F3', 2, false, false, 'ACTIVE',    'ACTIVE', false, NOW(), NOW(), 'system', 'system'),
        (gen_random_uuid(), 0, v_biz.id, 'Preparing',         'Order is being prepared',                   '#9C27B0', 3, false, false, 'ACTIVE',    'ACTIVE', false, NOW(), NOW(), 'system', 'system'),
        (gen_random_uuid(), 0, v_biz.id, 'Ready',             'Order is ready for pickup/delivery',        '#4CAF50', 4, false, false, 'ACTIVE',    'ACTIVE', false, NOW(), NOW(), 'system', 'system'),
        (gen_random_uuid(), 0, v_biz.id, 'Out for Delivery',  'Order is on the way',                       '#00BCD4', 5, false, false, 'ACTIVE',    'ACTIVE', false, NOW(), NOW(), 'system', 'system'),
        (gen_random_uuid(), 0, v_biz.id, 'Delivered',         'Order completed successfully',               '#8BC34A', 6, false, true,  'COMPLETED', 'ACTIVE', false, NOW(), NOW(), 'system', 'system'),
        (gen_random_uuid(), 0, v_biz.id, 'Cancelled',         'Order was cancelled',                        '#F44336', 7, false, true,  'CANCELLED', 'ACTIVE', false, NOW(), NOW(), 'system', 'system'),
        (gen_random_uuid(), 0, v_biz.id, 'Rejected',          'Order was rejected by business',             '#E91E63', 8, false, true,  'CANCELLED', 'ACTIVE', false, NOW(), NOW(), 'system', 'system');

        RAISE NOTICE 'Created 8 order process statuses for business: %', v_biz.name;
    END LOOP;
END $$;

-- =====================================================================
-- STEP 2: Generate 300 orders per business (with payments + history)
-- Loops ALL businesses dynamically
-- =====================================================================
DO $$
DECLARE
    v_biz RECORD;
    v_biz_counter INT := 0;
    v_order_id UUID;
    v_order_number TEXT;
    v_process_status_id UUID;
    v_payment_method TEXT;
    v_payment_methods TEXT[] := ARRAY['CASH', 'BANK_TRANSFER', 'ONLINE', 'OTHER'];
    v_customer_payment_methods TEXT[] := ARRAY['Cash', 'Card', 'ABA Pay', 'Wing', 'ACLEDA Mobile', 'TrueMoney'];
    v_guest_names TEXT[] := ARRAY[
        'Sokha', 'Dara', 'Vanna', 'Pisey', 'Chenda', 'Bopha', 'Ratana', 'Narith', 'Kosal', 'Maly',
        'Sophal', 'Thy', 'Visal', 'Channary', 'Pheakdey', 'Samnang', 'Kunthea', 'Rith', 'Serey', 'Thida'
    ];
    v_guest_phones TEXT[] := ARRAY[
        '012345001','012345002','012345003','012345004','012345005',
        '098765001','098765002','098765003','098765004','098765005',
        '077111001','077111002','077111003','077111004','077111005',
        '096222001','096222002','096222003','096222004','096222005'
    ];
    v_product_names_1 TEXT[] := ARRAY['Lok Lak', 'Amok Fish', 'Fried Rice', 'Pad Thai', 'Spring Rolls', 'Mango Smoothie', 'Iced Coffee', 'Green Curry', 'Pho Soup', 'Banh Mi'];
    v_product_names_2 TEXT[] := ARRAY['Tom Yum', 'Bubble Tea', 'Croissant', 'Caesar Salad', 'Chicken Wings', 'Fish and Chips', 'Cappuccino', 'Cheesecake', 'Sushi Roll', 'Noodle Soup'];
    v_size_names TEXT[] := ARRAY['Small', 'Medium', 'Large', 'Standard'];
    v_locations TEXT[] := ARRAY['Phnom Penh', 'Siem Reap', 'Battambang', 'Sihanoukville', 'Kampot', 'Kep', 'Pursat', 'Takeo'];
    v_subtotal NUMERIC(10,2);
    v_delivery_fee NUMERIC(10,2);
    v_total NUMERIC(10,2);
    v_is_pos BOOLEAN;
    v_is_guest BOOLEAN;
    v_is_paid BOOLEAN;
    v_payment_status TEXT;
    v_payment_ref TEXT;
    v_created_at TIMESTAMP;
    v_confirmed_at TIMESTAMP;
    v_completed_at TIMESTAMP;
    v_order_seq INT;
    v_statuses_for_biz UUID[];
    v_status_count INT;
    v_rand_status_idx INT;
    v_enum_status TEXT;
    v_enum_statuses TEXT[] := ARRAY['PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED', 'REJECTED'];
    v_default_status_id UUID;
    v_total_businesses INT;
    v_pct INT;
    i INT;
BEGIN
    -- Count total businesses for progress tracking
    SELECT COUNT(*) INTO v_total_businesses FROM businesses WHERE is_deleted = false;
    RAISE NOTICE '=== Starting order generation for % businesses (300 orders each) ===', v_total_businesses;

    -- Loop through ALL businesses in the database
    FOR v_biz IN
        SELECT id, name FROM businesses WHERE is_deleted = false ORDER BY name
    LOOP
        v_biz_counter := v_biz_counter + 1;

        -- Get all process statuses for this business (sorted by sort_order)
        SELECT ARRAY_AGG(id ORDER BY sort_order) INTO v_statuses_for_biz
        FROM order_process_statuses
        WHERE business_id = v_biz.id AND is_deleted = false;

        v_status_count := COALESCE(array_length(v_statuses_for_biz, 1), 0);

        -- Get default status
        SELECT id INTO v_default_status_id
        FROM order_process_statuses
        WHERE business_id = v_biz.id AND is_default = true AND is_deleted = false
        LIMIT 1;

        IF v_status_count = 0 THEN
            RAISE NOTICE '[%/%] Business "%" has no statuses, skipping...', v_biz_counter, v_total_businesses, v_biz.name;
            CONTINUE;
        END IF;

        RAISE NOTICE '[%/%] Business "%": generating 300 orders...', v_biz_counter, v_total_businesses, v_biz.name;

        -- Generate 300 orders for this business
        FOR i IN 1..300 LOOP
            -- Log progress every 5% (every 15 orders)
            IF i % 15 = 0 THEN
                v_pct := (i * 100) / 300;
                RAISE NOTICE '  -> %% complete (%/300 orders)', v_pct, i;
            END IF;

            v_order_seq := (v_biz_counter - 1) * 300 + i;
            v_order_id := gen_random_uuid();
            v_order_number := 'ORD-' || TO_CHAR(NOW(), 'YYYYMMDD') || '-' || LPAD(v_order_seq::TEXT, 6, '0');

            -- Random creation date within last 90 days
            v_created_at := NOW() - (random() * 90)::INT * INTERVAL '1 day'
                                  - (random() * 24)::INT * INTERVAL '1 hour'
                                  - (random() * 60)::INT * INTERVAL '1 minute';

            -- Random order type
            v_is_pos := (random() < 0.3);
            v_is_guest := v_is_pos OR (random() < 0.4);

            -- Random pricing ($2 - $80)
            v_subtotal := round((random() * 78 + 2)::NUMERIC, 2);
            v_delivery_fee := CASE WHEN random() < 0.4 THEN round((random() * 5 + 0.5)::NUMERIC, 2) ELSE 0.00 END;
            v_total := v_subtotal + v_delivery_fee;

            -- Random payment method
            v_payment_method := v_payment_methods[1 + floor(random() * 4)::INT];

            -- Random status distribution:
            -- 50% completed, 15% cancelled/rejected, 35% active (in-progress)
            IF random() < 0.50 THEN
                -- Completed orders
                v_enum_status := 'DELIVERED';
                v_is_paid := true;
                v_payment_status := 'COMPLETED';
                v_confirmed_at := v_created_at + INTERVAL '5 minutes';
                v_completed_at := v_created_at + (30 + floor(random() * 60)::INT) * INTERVAL '1 minute';
                v_process_status_id := NULL;
                IF v_status_count > 0 THEN
                    SELECT id INTO v_process_status_id FROM order_process_statuses
                    WHERE business_id = v_biz.id AND status_type = 'COMPLETED' AND is_deleted = false
                    LIMIT 1;
                END IF;
            ELSIF random() < 0.30 THEN
                -- Cancelled/Rejected
                IF random() < 0.5 THEN
                    v_enum_status := 'CANCELLED';
                ELSE
                    v_enum_status := 'REJECTED';
                END IF;
                v_is_paid := false;
                v_payment_status := 'CANCELLED';
                v_confirmed_at := NULL;
                v_completed_at := NULL;
                v_process_status_id := NULL;
                IF v_status_count > 0 THEN
                    SELECT id INTO v_process_status_id FROM order_process_statuses
                    WHERE business_id = v_biz.id AND status_type = 'CANCELLED' AND is_deleted = false
                    ORDER BY random() LIMIT 1;
                END IF;
            ELSE
                -- Active/In-progress orders (pick a random active status)
                v_rand_status_idx := 1 + floor(random() * GREATEST(v_status_count - 2, 1))::INT;
                v_enum_status := v_enum_statuses[LEAST(v_rand_status_idx, 5)];
                v_is_paid := (random() < 0.3);
                v_payment_status := CASE WHEN v_is_paid THEN 'COMPLETED' ELSE 'PENDING' END;
                v_confirmed_at := CASE WHEN v_rand_status_idx > 1 THEN v_created_at + INTERVAL '5 minutes' ELSE NULL END;
                v_completed_at := NULL;
                v_process_status_id := v_statuses_for_biz[LEAST(v_rand_status_idx, v_status_count)];
            END IF;

            -- Insert order
            INSERT INTO orders (
                id, version, order_number, customer_id, guest_phone, guest_name, guest_location,
                business_id, delivery_address_id, delivery_option_id,
                status, order_process_status_id,
                customer_note, business_note,
                subtotal, delivery_fee, total_amount,
                payment_method, is_paid, is_pos_order, is_guest_order,
                confirmed_at, completed_at,
                is_deleted, created_at, updated_at, created_by, updated_by
            ) VALUES (
                v_order_id, 0, v_order_number, NULL,
                v_guest_phones[1 + floor(random() * 20)::INT],
                v_guest_names[1 + floor(random() * 20)::INT],
                v_locations[1 + floor(random() * 8)::INT],
                v_biz.id, NULL, NULL,
                v_enum_status, v_process_status_id,
                CASE WHEN random() < 0.2 THEN 'Please make it spicy'
                     WHEN random() < 0.2 THEN 'No sugar please'
                     WHEN random() < 0.2 THEN 'Extra sauce on the side'
                     WHEN random() < 0.1 THEN 'Allergic to peanuts'
                     ELSE NULL END,
                CASE WHEN v_enum_status IN ('CANCELLED', 'REJECTED') THEN
                    (ARRAY['Sorry, item out of stock', 'Kitchen is closed', 'Too many orders right now', 'Ingredient unavailable'])[1 + floor(random() * 4)::INT]
                ELSE NULL END,
                v_subtotal, v_delivery_fee, v_total,
                v_payment_method, v_is_paid, v_is_pos, v_is_guest,
                v_confirmed_at, v_completed_at,
                false, v_created_at, v_created_at, 'system', 'system'
            );

            -- Insert 2 order items per order
            INSERT INTO order_items (
                id, version, order_id, product_id, product_size_id,
                product_name, product_image_url, size_name,
                unit_price, quantity, total_price,
                is_deleted, created_at, updated_at, created_by, updated_by
            ) VALUES
            (
                gen_random_uuid(), 0, v_order_id, NULL, NULL,
                v_product_names_1[1 + floor(random() * 10)::INT], NULL,
                v_size_names[1 + floor(random() * 4)::INT],
                round((v_subtotal * 0.6)::NUMERIC, 2), 1 + floor(random() * 3)::INT,
                round((v_subtotal * 0.6)::NUMERIC, 2),
                false, v_created_at, v_created_at, 'system', 'system'
            ),
            (
                gen_random_uuid(), 0, v_order_id, NULL, NULL,
                v_product_names_2[1 + floor(random() * 10)::INT], NULL,
                v_size_names[1 + floor(random() * 4)::INT],
                round((v_subtotal * 0.4)::NUMERIC, 2), 1 + floor(random() * 2)::INT,
                round((v_subtotal * 0.4)::NUMERIC, 2),
                false, v_created_at, v_created_at, 'system', 'system'
            );

            -- Insert payment record
            v_payment_ref := 'PAY-' || TO_CHAR(v_created_at, 'YYYYMMDD') || '-' || LPAD(v_order_seq::TEXT, 7, '0');

            INSERT INTO business_order_payments (
                id, version, business_id, order_id, payment_reference,
                amount, payment_method, status, customer_payment_method,
                is_deleted, created_at, updated_at, created_by, updated_by
            ) VALUES (
                gen_random_uuid(), 0, v_biz.id, v_order_id, v_payment_ref,
                v_total, v_payment_method, v_payment_status,
                v_customer_payment_methods[1 + floor(random() * 6)::INT],
                false, v_created_at, v_created_at, 'system', 'system'
            );

            -- Insert order status history: initial PENDING
            INSERT INTO order_status_history (
                id, version, order_id, status, order_process_status_id, note, changed_by,
                is_deleted, created_at, updated_at, created_by, updated_by
            ) VALUES (
                gen_random_uuid(), 0, v_order_id, 'PENDING', v_default_status_id,
                'Order placed', 'system',
                false, v_created_at, v_created_at, 'system', 'system'
            );

            -- Add progressive status history for non-pending orders
            IF v_enum_status != 'PENDING' THEN
                -- CONFIRMED
                IF v_enum_status IN ('CONFIRMED', 'PREPARING', 'READY', 'OUT_FOR_DELIVERY', 'DELIVERED') THEN
                    INSERT INTO order_status_history (
                        id, version, order_id, status, order_process_status_id, note, changed_by,
                        is_deleted, created_at, updated_at, created_by, updated_by
                    ) VALUES (
                        gen_random_uuid(), 0, v_order_id, 'CONFIRMED',
                        CASE WHEN v_status_count >= 2 THEN v_statuses_for_biz[2] ELSE NULL END,
                        'Order confirmed by business', 'business_staff',
                        false, v_created_at + INTERVAL '5 minutes', v_created_at + INTERVAL '5 minutes', 'system', 'system'
                    );
                END IF;

                -- PREPARING
                IF v_enum_status IN ('PREPARING', 'READY', 'OUT_FOR_DELIVERY', 'DELIVERED') THEN
                    INSERT INTO order_status_history (
                        id, version, order_id, status, order_process_status_id, note, changed_by,
                        is_deleted, created_at, updated_at, created_by, updated_by
                    ) VALUES (
                        gen_random_uuid(), 0, v_order_id, 'PREPARING',
                        CASE WHEN v_status_count >= 3 THEN v_statuses_for_biz[3] ELSE NULL END,
                        'Order is being prepared', 'kitchen_staff',
                        false, v_created_at + INTERVAL '10 minutes', v_created_at + INTERVAL '10 minutes', 'system', 'system'
                    );
                END IF;

                -- READY
                IF v_enum_status IN ('READY', 'OUT_FOR_DELIVERY', 'DELIVERED') THEN
                    INSERT INTO order_status_history (
                        id, version, order_id, status, order_process_status_id, note, changed_by,
                        is_deleted, created_at, updated_at, created_by, updated_by
                    ) VALUES (
                        gen_random_uuid(), 0, v_order_id, 'READY',
                        CASE WHEN v_status_count >= 4 THEN v_statuses_for_biz[4] ELSE NULL END,
                        'Order is ready', 'kitchen_staff',
                        false, v_created_at + INTERVAL '25 minutes', v_created_at + INTERVAL '25 minutes', 'system', 'system'
                    );
                END IF;

                -- OUT_FOR_DELIVERY
                IF v_enum_status IN ('OUT_FOR_DELIVERY', 'DELIVERED') THEN
                    INSERT INTO order_status_history (
                        id, version, order_id, status, order_process_status_id, note, changed_by,
                        is_deleted, created_at, updated_at, created_by, updated_by
                    ) VALUES (
                        gen_random_uuid(), 0, v_order_id, 'OUT_FOR_DELIVERY',
                        CASE WHEN v_status_count >= 5 THEN v_statuses_for_biz[5] ELSE NULL END,
                        'Order is out for delivery', 'delivery_staff',
                        false, v_created_at + INTERVAL '30 minutes', v_created_at + INTERVAL '30 minutes', 'system', 'system'
                    );
                END IF;

                -- DELIVERED
                IF v_enum_status = 'DELIVERED' THEN
                    INSERT INTO order_status_history (
                        id, version, order_id, status, order_process_status_id, note, changed_by,
                        is_deleted, created_at, updated_at, created_by, updated_by
                    ) VALUES (
                        gen_random_uuid(), 0, v_order_id, 'DELIVERED',
                        v_process_status_id,
                        'Order delivered successfully', 'delivery_staff',
                        false, v_completed_at, v_completed_at, 'system', 'system'
                    );
                END IF;

                -- CANCELLED / REJECTED
                IF v_enum_status IN ('CANCELLED', 'REJECTED') THEN
                    INSERT INTO order_status_history (
                        id, version, order_id, status, order_process_status_id, note, changed_by,
                        is_deleted, created_at, updated_at, created_by, updated_by
                    ) VALUES (
                        gen_random_uuid(), 0, v_order_id, v_enum_status,
                        v_process_status_id,
                        CASE WHEN v_enum_status = 'CANCELLED' THEN 'Customer cancelled the order'
                             ELSE 'Business rejected - item unavailable' END,
                        CASE WHEN v_enum_status = 'CANCELLED' THEN 'customer' ELSE 'business_staff' END,
                        false, v_created_at + INTERVAL '3 minutes', v_created_at + INTERVAL '3 minutes', 'system', 'system'
                    );
                END IF;
            END IF;

        END LOOP;

        RAISE NOTICE '[%/%] Business "%" DONE (300 orders + payments + history)', v_biz_counter, v_total_businesses, v_biz.name;
    END LOOP;

    RAISE NOTICE '============================================================';
    RAISE NOTICE '  COMPLETED: % businesses x 300 orders = % total orders', v_biz_counter, v_biz_counter * 300;
    RAISE NOTICE '  + % payments + status history records', v_biz_counter * 300;
    RAISE NOTICE '============================================================';
END $$;

-- =====================================================================
-- STEP 3: Verification queries
-- =====================================================================

-- Check order process statuses per business
SELECT b.name AS business_name, ops.name AS status_name, ops.description, ops.color,
       ops.sort_order, ops.is_default, ops.is_final, ops.status_type
FROM order_process_statuses ops
JOIN businesses b ON b.id = ops.business_id
WHERE ops.is_deleted = false
ORDER BY b.name, ops.sort_order;

-- Count orders per business
SELECT b.name AS business_name, COUNT(o.id) AS total_orders
FROM orders o
JOIN businesses b ON b.id = o.business_id
WHERE o.is_deleted = false
GROUP BY b.name
ORDER BY b.name;

-- Count orders per status per business
SELECT b.name AS business_name, o.status, COUNT(*) AS count
FROM orders o
JOIN businesses b ON b.id = o.business_id
WHERE o.is_deleted = false
GROUP BY b.name, o.status
ORDER BY b.name, o.status;

-- Count payments per business
SELECT b.name AS business_name, bop.status AS payment_status, COUNT(*) AS count,
       COALESCE(SUM(bop.amount), 0) AS total_amount
FROM business_order_payments bop
JOIN businesses b ON b.id = bop.business_id
WHERE bop.is_deleted = false
GROUP BY b.name, bop.status
ORDER BY b.name, bop.status;

-- Count order status history records per business
SELECT b.name AS business_name, COUNT(osh.id) AS history_count
FROM order_status_history osh
JOIN orders o ON o.id = osh.order_id
JOIN businesses b ON b.id = o.business_id
WHERE osh.is_deleted = false
GROUP BY b.name
ORDER BY b.name;

-- Sample: view orders with their dynamic process status
SELECT o.order_number, b.name AS business_name, o.status AS enum_status,
       ops.name AS process_status, ops.color, ops.status_type,
       o.total_amount, o.is_paid, o.created_at
FROM orders o
JOIN businesses b ON b.id = o.business_id
LEFT JOIN order_process_statuses ops ON ops.id = o.order_process_status_id
WHERE o.is_deleted = false
ORDER BY o.created_at DESC
LIMIT 30;
