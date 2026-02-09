-- =====================================================================
-- E-Menu Platform: Dynamic Order Process Status + Test Data Script
-- Run this in pgAdmin for testing
-- =====================================================================

-- =====================================================================
-- STEP 1: Create 3 test businesses (if not exist)
-- =====================================================================
DO $$
DECLARE
    v_business1_id UUID := 'a0000001-0000-0000-0000-000000000001';
    v_business2_id UUID := 'a0000002-0000-0000-0000-000000000002';
    v_business3_id UUID := 'a0000003-0000-0000-0000-000000000003';
    v_owner_id UUID;
BEGIN
    -- Get an existing owner (first platform user)
    SELECT id INTO v_owner_id FROM users WHERE is_deleted = false LIMIT 1;

    IF v_owner_id IS NULL THEN
        RAISE NOTICE 'No users found. Please create at least one user first.';
        RETURN;
    END IF;

    -- Business 1: Restaurant
    INSERT INTO businesses (id, version, name, email, phone, address, description, owner_id, status, is_subscription_active, is_deleted, created_at, updated_at, created_by, updated_by)
    VALUES (v_business1_id, 0, 'Khmer Kitchen Restaurant', 'khmer.kitchen@emenu.com', '012345678', 'Phnom Penh, Cambodia', 'Traditional Khmer restaurant', v_owner_id, 'ACTIVE', true, false, NOW(), NOW(), 'system', 'system')
    ON CONFLICT (id) DO NOTHING;

    -- Business 2: Cafe
    INSERT INTO businesses (id, version, name, email, phone, address, description, owner_id, status, is_subscription_active, is_deleted, created_at, updated_at, created_by, updated_by)
    VALUES (v_business2_id, 0, 'Sunrise Coffee Shop', 'sunrise.coffee@emenu.com', '098765432', 'Siem Reap, Cambodia', 'Modern coffee shop and bakery', v_owner_id, 'ACTIVE', true, false, NOW(), NOW(), 'system', 'system')
    ON CONFLICT (id) DO NOTHING;

    -- Business 3: Fast Food
    INSERT INTO businesses (id, version, name, email, phone, address, description, owner_id, status, is_subscription_active, is_deleted, created_at, updated_at, created_by, updated_by)
    VALUES (v_business3_id, 0, 'Quick Bite Express', 'quickbite@emenu.com', '077112233', 'Battambang, Cambodia', 'Fast food and delivery', v_owner_id, 'ACTIVE', true, false, NOW(), NOW(), 'system', 'system')
    ON CONFLICT (id) DO NOTHING;
END $$;

-- =====================================================================
-- STEP 2: Insert dynamic order process statuses per business
-- Each business gets their own custom workflow
-- =====================================================================

-- Business 1: Khmer Kitchen Restaurant (full restaurant workflow)
INSERT INTO order_process_statuses (id, version, business_id, name, description, color, sort_order, is_default, is_final, status_type, status, is_deleted, created_at, updated_at, created_by, updated_by) VALUES
(gen_random_uuid(), 0, 'a0000001-0000-0000-0000-000000000001', 'Pending', 'Order received, waiting for confirmation', '#FFA500', 1, true, false, 'ACTIVE', 'ACTIVE', false, NOW(), NOW(), 'system', 'system'),
(gen_random_uuid(), 0, 'a0000001-0000-0000-0000-000000000001', 'Confirmed', 'Order accepted by kitchen', '#2196F3', 2, false, false, 'ACTIVE', 'ACTIVE', false, NOW(), NOW(), 'system', 'system'),
(gen_random_uuid(), 0, 'a0000001-0000-0000-0000-000000000001', 'Preparing', 'Chef is preparing the food', '#9C27B0', 3, false, false, 'ACTIVE', 'ACTIVE', false, NOW(), NOW(), 'system', 'system'),
(gen_random_uuid(), 0, 'a0000001-0000-0000-0000-000000000001', 'Ready to Serve', 'Food is ready to be served', '#4CAF50', 4, false, false, 'ACTIVE', 'ACTIVE', false, NOW(), NOW(), 'system', 'system'),
(gen_random_uuid(), 0, 'a0000001-0000-0000-0000-000000000001', 'Served', 'Order has been served to customer', '#009688', 5, false, false, 'ACTIVE', 'ACTIVE', false, NOW(), NOW(), 'system', 'system'),
(gen_random_uuid(), 0, 'a0000001-0000-0000-0000-000000000001', 'Completed', 'Order completed and paid', '#8BC34A', 6, false, true, 'COMPLETED', 'ACTIVE', false, NOW(), NOW(), 'system', 'system'),
(gen_random_uuid(), 0, 'a0000001-0000-0000-0000-000000000001', 'Cancelled', 'Order was cancelled', '#F44336', 7, false, true, 'CANCELLED', 'ACTIVE', false, NOW(), NOW(), 'system', 'system'),
(gen_random_uuid(), 0, 'a0000001-0000-0000-0000-000000000001', 'Rejected', 'Order was rejected by kitchen', '#E91E63', 8, false, true, 'CANCELLED', 'ACTIVE', false, NOW(), NOW(), 'system', 'system');

-- Business 2: Sunrise Coffee Shop (simpler cafe workflow)
INSERT INTO order_process_statuses (id, version, business_id, name, description, color, sort_order, is_default, is_final, status_type, status, is_deleted, created_at, updated_at, created_by, updated_by) VALUES
(gen_random_uuid(), 0, 'a0000002-0000-0000-0000-000000000002', 'New Order', 'New order placed', '#FF9800', 1, true, false, 'ACTIVE', 'ACTIVE', false, NOW(), NOW(), 'system', 'system'),
(gen_random_uuid(), 0, 'a0000002-0000-0000-0000-000000000002', 'Making', 'Barista is making the order', '#673AB7', 2, false, false, 'ACTIVE', 'ACTIVE', false, NOW(), NOW(), 'system', 'system'),
(gen_random_uuid(), 0, 'a0000002-0000-0000-0000-000000000002', 'Ready for Pickup', 'Order is ready at counter', '#4CAF50', 3, false, false, 'ACTIVE', 'ACTIVE', false, NOW(), NOW(), 'system', 'system'),
(gen_random_uuid(), 0, 'a0000002-0000-0000-0000-000000000002', 'Picked Up', 'Customer picked up the order', '#2196F3', 4, false, true, 'COMPLETED', 'ACTIVE', false, NOW(), NOW(), 'system', 'system'),
(gen_random_uuid(), 0, 'a0000002-0000-0000-0000-000000000002', 'Cancelled', 'Order cancelled', '#F44336', 5, false, true, 'CANCELLED', 'ACTIVE', false, NOW(), NOW(), 'system', 'system');

-- Business 3: Quick Bite Express (delivery-focused workflow)
INSERT INTO order_process_statuses (id, version, business_id, name, description, color, sort_order, is_default, is_final, status_type, status, is_deleted, created_at, updated_at, created_by, updated_by) VALUES
(gen_random_uuid(), 0, 'a0000003-0000-0000-0000-000000000003', 'Order Received', 'Order has been received', '#FF9800', 1, true, false, 'ACTIVE', 'ACTIVE', false, NOW(), NOW(), 'system', 'system'),
(gen_random_uuid(), 0, 'a0000003-0000-0000-0000-000000000003', 'Accepted', 'Order accepted by restaurant', '#2196F3', 2, false, false, 'ACTIVE', 'ACTIVE', false, NOW(), NOW(), 'system', 'system'),
(gen_random_uuid(), 0, 'a0000003-0000-0000-0000-000000000003', 'In Kitchen', 'Order is being prepared', '#9C27B0', 3, false, false, 'ACTIVE', 'ACTIVE', false, NOW(), NOW(), 'system', 'system'),
(gen_random_uuid(), 0, 'a0000003-0000-0000-0000-000000000003', 'Ready', 'Order is packed and ready', '#4CAF50', 4, false, false, 'ACTIVE', 'ACTIVE', false, NOW(), NOW(), 'system', 'system'),
(gen_random_uuid(), 0, 'a0000003-0000-0000-0000-000000000003', 'Out for Delivery', 'Driver is delivering the order', '#00BCD4', 5, false, false, 'ACTIVE', 'ACTIVE', false, NOW(), NOW(), 'system', 'system'),
(gen_random_uuid(), 0, 'a0000003-0000-0000-0000-000000000003', 'Delivered', 'Order delivered successfully', '#8BC34A', 6, false, true, 'COMPLETED', 'ACTIVE', false, NOW(), NOW(), 'system', 'system'),
(gen_random_uuid(), 0, 'a0000003-0000-0000-0000-000000000003', 'Cancelled', 'Order was cancelled', '#F44336', 7, false, true, 'CANCELLED', 'ACTIVE', false, NOW(), NOW(), 'system', 'system'),
(gen_random_uuid(), 0, 'a0000003-0000-0000-0000-000000000003', 'Refunded', 'Order was refunded', '#795548', 8, false, true, 'CANCELLED', 'ACTIVE', false, NOW(), NOW(), 'system', 'system');

-- =====================================================================
-- STEP 3: Generate 300 orders (100 per business) with payments
-- and order_status_history records
-- =====================================================================
DO $$
DECLARE
    v_business_ids UUID[] := ARRAY[
        'a0000001-0000-0000-0000-000000000001'::UUID,
        'a0000002-0000-0000-0000-000000000002'::UUID,
        'a0000003-0000-0000-0000-000000000003'::UUID
    ];
    v_business_id UUID;
    v_order_id UUID;
    v_order_number TEXT;
    v_status TEXT;
    v_process_status_id UUID;
    v_payment_method TEXT;
    v_payment_methods TEXT[] := ARRAY['CASH', 'BANK_TRANSFER', 'ONLINE', 'OTHER'];
    v_customer_payment_methods TEXT[] := ARRAY['Cash', 'Card', 'ABA Pay', 'Wing', 'ACLEDA Mobile', 'TrueMoney'];
    v_guest_names TEXT[] := ARRAY['Sokha', 'Dara', 'Vanna', 'Pisey', 'Chenda', 'Bopha', 'Ratana', 'Narith', 'Kosal', 'Maly',
                                   'Sophal', 'Thy', 'Visal', 'Channary', 'Pheakdey', 'Samnang', 'Kunthea', 'Rith', 'Serey', 'Thida'];
    v_guest_phones TEXT[] := ARRAY['012345001','012345002','012345003','012345004','012345005',
                                    '098765001','098765002','098765003','098765004','098765005',
                                    '077111001','077111002','077111003','077111004','077111005',
                                    '096222001','096222002','096222003','096222004','096222005'];
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
    v_biz_idx INT;
    i INT;
BEGIN
    FOR v_biz_idx IN 1..3 LOOP
        v_business_id := v_business_ids[v_biz_idx];

        -- Get all process statuses for this business
        SELECT ARRAY_AGG(id ORDER BY sort_order) INTO v_statuses_for_biz
        FROM order_process_statuses
        WHERE business_id = v_business_id AND is_deleted = false;

        v_status_count := array_length(v_statuses_for_biz, 1);

        FOR i IN 1..100 LOOP
            v_order_seq := (v_biz_idx - 1) * 100 + i;
            v_order_id := gen_random_uuid();
            v_order_number := 'ORD-' || TO_CHAR(NOW(), 'YYYYMMDD') || '-' || LPAD(v_order_seq::TEXT, 4, '0');

            -- Random creation date within last 90 days
            v_created_at := NOW() - (random() * 90)::INT * INTERVAL '1 day' - (random() * 24)::INT * INTERVAL '1 hour';

            -- Random order type
            v_is_pos := (random() < 0.3);
            v_is_guest := v_is_pos OR (random() < 0.4);

            -- Random pricing
            v_subtotal := round((random() * 45 + 5)::NUMERIC, 2);
            v_delivery_fee := CASE WHEN random() < 0.5 THEN round((random() * 3 + 1)::NUMERIC, 2) ELSE 0.00 END;
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
                v_completed_at := v_created_at + INTERVAL '45 minutes';
                -- Pick the final COMPLETED status
                v_process_status_id := NULL;
                IF v_status_count IS NOT NULL THEN
                    SELECT id INTO v_process_status_id FROM order_process_statuses
                    WHERE business_id = v_business_id AND status_type = 'COMPLETED' AND is_deleted = false
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
                IF v_status_count IS NOT NULL THEN
                    SELECT id INTO v_process_status_id FROM order_process_statuses
                    WHERE business_id = v_business_id AND status_type = 'CANCELLED' AND is_deleted = false
                    ORDER BY random() LIMIT 1;
                END IF;
            ELSE
                -- Active/In-progress orders
                v_rand_status_idx := 1 + floor(random() * LEAST(v_status_count - 2, 4))::INT;
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
                CASE WHEN random() < 0.5 THEN 'Phnom Penh' WHEN random() < 0.5 THEN 'Siem Reap' ELSE 'Battambang' END,
                v_business_id, NULL, NULL,
                v_enum_status, v_process_status_id,
                CASE WHEN random() < 0.3 THEN 'Please make it spicy' WHEN random() < 0.3 THEN 'No sugar please' ELSE NULL END,
                CASE WHEN v_enum_status IN ('CANCELLED', 'REJECTED') THEN 'Sorry, item out of stock' ELSE NULL END,
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
                (ARRAY['Lok Lak', 'Amok Fish', 'Fried Rice', 'Pad Thai', 'Spring Rolls', 'Mango Smoothie', 'Iced Coffee', 'Green Curry', 'Pho Soup', 'Banh Mi'])[1 + floor(random() * 10)::INT],
                NULL,
                (ARRAY['Small', 'Medium', 'Large', 'Standard'])[1 + floor(random() * 4)::INT],
                round((v_subtotal * 0.6)::NUMERIC, 2), 1, round((v_subtotal * 0.6)::NUMERIC, 2),
                false, v_created_at, v_created_at, 'system', 'system'
            ),
            (
                gen_random_uuid(), 0, v_order_id, NULL, NULL,
                (ARRAY['Tom Yum', 'Bubble Tea', 'Croissant', 'Caesar Salad', 'Chicken Wings', 'Fish and Chips', 'Cappuccino', 'Cheesecake', 'Sushi Roll', 'Noodle Soup'])[1 + floor(random() * 10)::INT],
                NULL,
                (ARRAY['Small', 'Medium', 'Large', 'Standard'])[1 + floor(random() * 4)::INT],
                round((v_subtotal * 0.4)::NUMERIC, 2), 1, round((v_subtotal * 0.4)::NUMERIC, 2),
                false, v_created_at, v_created_at, 'system', 'system'
            );

            -- Insert payment record
            v_payment_ref := 'PAY-' || TO_CHAR(v_created_at, 'YYYYMMDD') || '-' || LPAD(v_order_seq::TEXT, 5, '0');

            INSERT INTO business_order_payments (
                id, version, business_id, order_id, payment_reference,
                amount, payment_method, status, customer_payment_method,
                is_deleted, created_at, updated_at, created_by, updated_by
            ) VALUES (
                gen_random_uuid(), 0, v_business_id, v_order_id, v_payment_ref,
                v_total, v_payment_method, v_payment_status,
                v_customer_payment_methods[1 + floor(random() * 6)::INT],
                false, v_created_at, v_created_at, 'system', 'system'
            );

            -- Insert order status history (initial status)
            INSERT INTO order_status_history (
                id, version, order_id, status, order_process_status_id, note, changed_by,
                is_deleted, created_at, updated_at, created_by, updated_by
            ) VALUES (
                gen_random_uuid(), 0, v_order_id, 'PENDING',
                (SELECT id FROM order_process_statuses WHERE business_id = v_business_id AND is_default = true AND is_deleted = false LIMIT 1),
                'Order placed', 'system',
                false, v_created_at, v_created_at, 'system', 'system'
            );

            -- Add more status history entries for non-pending orders
            IF v_enum_status != 'PENDING' THEN
                IF v_enum_status IN ('CONFIRMED', 'PREPARING', 'READY', 'OUT_FOR_DELIVERY', 'DELIVERED') THEN
                    INSERT INTO order_status_history (
                        id, version, order_id, status, order_process_status_id, note, changed_by,
                        is_deleted, created_at, updated_at, created_by, updated_by
                    ) VALUES (
                        gen_random_uuid(), 0, v_order_id, 'CONFIRMED',
                        (SELECT id FROM order_process_statuses WHERE business_id = v_business_id AND sort_order = 2 AND is_deleted = false LIMIT 1),
                        'Order confirmed by business', 'business_staff',
                        false, v_created_at + INTERVAL '5 minutes', v_created_at + INTERVAL '5 minutes', 'system', 'system'
                    );
                END IF;

                IF v_enum_status IN ('PREPARING', 'READY', 'OUT_FOR_DELIVERY', 'DELIVERED') THEN
                    INSERT INTO order_status_history (
                        id, version, order_id, status, order_process_status_id, note, changed_by,
                        is_deleted, created_at, updated_at, created_by, updated_by
                    ) VALUES (
                        gen_random_uuid(), 0, v_order_id, 'PREPARING',
                        (SELECT id FROM order_process_statuses WHERE business_id = v_business_id AND sort_order = 3 AND is_deleted = false LIMIT 1),
                        'Order is being prepared', 'kitchen_staff',
                        false, v_created_at + INTERVAL '10 minutes', v_created_at + INTERVAL '10 minutes', 'system', 'system'
                    );
                END IF;

                IF v_enum_status IN ('READY', 'OUT_FOR_DELIVERY', 'DELIVERED') THEN
                    INSERT INTO order_status_history (
                        id, version, order_id, status, order_process_status_id, note, changed_by,
                        is_deleted, created_at, updated_at, created_by, updated_by
                    ) VALUES (
                        gen_random_uuid(), 0, v_order_id, 'READY',
                        (SELECT id FROM order_process_statuses WHERE business_id = v_business_id AND sort_order = 4 AND is_deleted = false LIMIT 1),
                        'Order is ready', 'kitchen_staff',
                        false, v_created_at + INTERVAL '25 minutes', v_created_at + INTERVAL '25 minutes', 'system', 'system'
                    );
                END IF;

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

                IF v_enum_status IN ('CANCELLED', 'REJECTED') THEN
                    INSERT INTO order_status_history (
                        id, version, order_id, status, order_process_status_id, note, changed_by,
                        is_deleted, created_at, updated_at, created_by, updated_by
                    ) VALUES (
                        gen_random_uuid(), 0, v_order_id, v_enum_status,
                        v_process_status_id,
                        CASE WHEN v_enum_status = 'CANCELLED' THEN 'Customer cancelled the order' ELSE 'Business rejected - item unavailable' END,
                        CASE WHEN v_enum_status = 'CANCELLED' THEN 'customer' ELSE 'business_staff' END,
                        false, v_created_at + INTERVAL '3 minutes', v_created_at + INTERVAL '3 minutes', 'system', 'system'
                    );
                END IF;
            END IF;

        END LOOP;

        RAISE NOTICE 'Generated 100 orders for business %', v_business_id;
    END LOOP;

    RAISE NOTICE 'Successfully generated 300 orders with payments and status history!';
END $$;

-- =====================================================================
-- STEP 4: Verification queries
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
AND o.order_number LIKE 'ORD-%'
GROUP BY b.name
ORDER BY b.name;

-- Count orders per status per business
SELECT b.name AS business_name, o.status, COUNT(*) AS count
FROM orders o
JOIN businesses b ON b.id = o.business_id
WHERE o.is_deleted = false
AND o.order_number LIKE 'ORD-%'
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

-- Count order status history records
SELECT b.name AS business_name, COUNT(osh.id) AS history_count
FROM order_status_history osh
JOIN orders o ON o.id = osh.order_id
JOIN businesses b ON b.id = o.business_id
WHERE osh.is_deleted = false
GROUP BY b.name
ORDER BY b.name;

-- Sample: view orders with their dynamic process status
SELECT o.order_number, o.status AS enum_status, ops.name AS process_status,
       ops.color, ops.status_type, o.total_amount, o.is_paid, o.created_at
FROM orders o
LEFT JOIN order_process_statuses ops ON ops.id = o.order_process_status_id
WHERE o.is_deleted = false
ORDER BY o.created_at DESC
LIMIT 20;
