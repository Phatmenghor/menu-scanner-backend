/*
 * ============================================================================
 * Script: 03_insert_order_related_data.sql
 * Description: Populates order_items, order_status_history, and business_order_payments
 *             for all existing orders
 * Prerequisites:
 *   - 01_insert_base_data.sql must be run first
 *   - 02_insert_7000_orders.sql must be run first
 * ============================================================================
 */

DO $$
DECLARE
    v_order RECORD;
    v_product RECORD;
    v_status RECORD;
    v_item_count INTEGER;
    v_quantity INTEGER;
    v_unit_price NUMERIC(10,2);
    v_total_price NUMERIC(10,2);
    v_has_promotion BOOLEAN;
    v_promotion_type VARCHAR(50);
    v_promotion_value NUMERIC(10,2);
    v_special_instructions TEXT;
    v_payment_reference VARCHAR(100);
    v_order_subtotal NUMERIC(10,2);
    v_status_timestamp TIMESTAMP;
    v_counter INTEGER := 0;
    v_total_orders INTEGER;
BEGIN
    -- Get total order count for progress tracking
    SELECT COUNT(*) INTO v_total_orders FROM orders WHERE NOT is_deleted;

    RAISE NOTICE 'Starting to populate order related data for % orders...', v_total_orders;

    -- Loop through all orders
    FOR v_order IN
        SELECT
            id,
            order_number,
            business_id,
            order_process_status_name,
            payment_method,
            payment_status,
            subtotal,
            total_amount,
            created_at,
            confirmed_at,
            completed_at
        FROM orders
        WHERE NOT is_deleted
        ORDER BY created_at
    LOOP
        v_counter := v_counter + 1;
        v_order_subtotal := 0;

        -- Progress indicator every 500 orders
        IF v_counter % 500 = 0 THEN
            RAISE NOTICE 'Processed % / % orders (%.1f%%)',
                v_counter, v_total_orders, (v_counter::NUMERIC / v_total_orders * 100);
        END IF;

        -- ========================================================================
        -- 1. INSERT ORDER ITEMS (1-5 items per order)
        -- ========================================================================
        v_item_count := (FLOOR(RANDOM() * 5) + 1)::INTEGER; -- 1 to 5 items

        FOR i IN 1..v_item_count LOOP
            -- Get a random product from the same business
            SELECT
                p.id,
                p.name,
                p.image_url,
                COALESCE(ps.size_price, p.price) as price,
                ps.id as size_id,
                ps.name as size_name
            INTO v_product
            FROM products p
            LEFT JOIN product_sizes ps ON p.id = ps.product_id
            WHERE p.business_id = v_order.business_id
                AND NOT p.is_deleted
                AND p.is_available
            ORDER BY RANDOM()
            LIMIT 1;

            IF v_product.id IS NOT NULL THEN
                -- Random quantity (1-3)
                v_quantity := (FLOOR(RANDOM() * 3) + 1)::INTEGER;

                -- Determine if item has promotion (30% chance)
                v_has_promotion := RANDOM() < 0.30;

                IF v_has_promotion THEN
                    -- Random promotion type
                    IF RANDOM() < 0.5 THEN
                        v_promotion_type := 'PERCENTAGE';
                        v_promotion_value := FLOOR(RANDOM() * 20 + 5)::NUMERIC; -- 5-25%
                        v_unit_price := v_product.price * (1 - v_promotion_value / 100);
                    ELSE
                        v_promotion_type := 'FIXED_AMOUNT';
                        v_promotion_value := FLOOR(RANDOM() * 3 + 1)::NUMERIC; -- $1-4
                        v_unit_price := GREATEST(v_product.price - v_promotion_value, v_product.price * 0.5);
                    END IF;
                ELSE
                    v_promotion_type := NULL;
                    v_promotion_value := NULL;
                    v_unit_price := v_product.price;
                END IF;

                v_total_price := v_unit_price * v_quantity;
                v_order_subtotal := v_order_subtotal + v_total_price;

                -- Random special instructions (20% chance)
                CASE
                    WHEN RANDOM() < 0.20 AND RANDOM() < 0.33 THEN v_special_instructions := 'Extra sauce on the side';
                    WHEN RANDOM() < 0.20 AND RANDOM() < 0.66 THEN v_special_instructions := 'No vegetables please';
                    WHEN RANDOM() < 0.20 THEN v_special_instructions := 'Make it less spicy';
                    ELSE v_special_instructions := NULL;
                END CASE;

                -- Insert order item
                INSERT INTO order_items (
                    id,
                    order_id,
                    product_id,
                    product_size_id,
                    product_name,
                    product_image_url,
                    size_name,
                    current_price,
                    final_price,
                    unit_price,
                    has_promotion,
                    promotion_type,
                    promotion_value,
                    quantity,
                    total_price,
                    special_instructions,
                    version,
                    created_at,
                    updated_at,
                    is_deleted
                ) VALUES (
                    gen_random_uuid(),
                    v_order.id,
                    v_product.id,
                    v_product.size_id,
                    v_product.name,
                    v_product.image_url,
                    COALESCE(v_product.size_name, 'Standard'),
                    v_product.price,
                    v_unit_price,
                    v_unit_price,
                    v_has_promotion,
                    v_promotion_type,
                    v_promotion_value,
                    v_quantity,
                    v_total_price,
                    v_special_instructions,
                    0,
                    v_order.created_at,
                    v_order.created_at,
                    false
                );
            END IF;
        END LOOP;

        -- ========================================================================
        -- 2. INSERT ORDER STATUS HISTORY
        -- ========================================================================
        -- Create status history based on order's current status
        v_status_timestamp := v_order.created_at;

        -- Pending status (always first)
        INSERT INTO order_status_history (
            id,
            order_id,
            order_process_status_id,
            note,
            changed_by,
            version,
            created_at,
            updated_at,
            is_deleted
        )
        SELECT
            gen_random_uuid(),
            v_order.id,
            ops.id,
            'Order placed',
            'system',
            0,
            v_status_timestamp,
            v_status_timestamp,
            false
        FROM order_process_statuses ops
        WHERE ops.name = 'Pending'
        LIMIT 1;

        -- Add subsequent status changes based on current status
        IF v_order.order_process_status_name IN ('Confirmed', 'Preparing', 'Ready', 'Out for Delivery', 'Delivered', 'Completed') THEN
            v_status_timestamp := v_status_timestamp + INTERVAL '5 minutes';
            INSERT INTO order_status_history (
                id, order_id, order_process_status_id, note, changed_by,
                version, created_at, updated_at, is_deleted
            )
            SELECT
                gen_random_uuid(), v_order.id, ops.id, 'Order confirmed by restaurant', 'business',
                0, v_status_timestamp, v_status_timestamp, false
            FROM order_process_statuses ops WHERE ops.name = 'Confirmed' LIMIT 1;
        END IF;

        IF v_order.order_process_status_name IN ('Preparing', 'Ready', 'Out for Delivery', 'Delivered', 'Completed') THEN
            v_status_timestamp := v_status_timestamp + INTERVAL '10 minutes';
            INSERT INTO order_status_history (
                id, order_id, order_process_status_id, note, changed_by,
                version, created_at, updated_at, is_deleted
            )
            SELECT
                gen_random_uuid(), v_order.id, ops.id, 'Preparing your order', 'business',
                0, v_status_timestamp, v_status_timestamp, false
            FROM order_process_statuses ops WHERE ops.name = 'Preparing' LIMIT 1;
        END IF;

        IF v_order.order_process_status_name IN ('Ready', 'Out for Delivery', 'Delivered', 'Completed') THEN
            v_status_timestamp := v_status_timestamp + INTERVAL '15 minutes';
            INSERT INTO order_status_history (
                id, order_id, order_process_status_id, note, changed_by,
                version, created_at, updated_at, is_deleted
            )
            SELECT
                gen_random_uuid(), v_order.id, ops.id, 'Order is ready', 'business',
                0, v_status_timestamp, v_status_timestamp, false
            FROM order_process_statuses ops WHERE ops.name = 'Ready' LIMIT 1;
        END IF;

        IF v_order.order_process_status_name IN ('Out for Delivery', 'Delivered', 'Completed') THEN
            v_status_timestamp := v_status_timestamp + INTERVAL '5 minutes';
            INSERT INTO order_status_history (
                id, order_id, order_process_status_id, note, changed_by,
                version, created_at, updated_at, is_deleted
            )
            SELECT
                gen_random_uuid(), v_order.id, ops.id, 'Out for delivery', 'delivery_driver',
                0, v_status_timestamp, v_status_timestamp, false
            FROM order_process_statuses ops WHERE ops.name = 'Out for Delivery' LIMIT 1;
        END IF;

        IF v_order.order_process_status_name IN ('Delivered', 'Completed') THEN
            v_status_timestamp := v_status_timestamp + INTERVAL '20 minutes';
            INSERT INTO order_status_history (
                id, order_id, order_process_status_id, note, changed_by,
                version, created_at, updated_at, is_deleted
            )
            SELECT
                gen_random_uuid(), v_order.id, ops.id, 'Order delivered', 'delivery_driver',
                0, v_status_timestamp, v_status_timestamp, false
            FROM order_process_statuses ops WHERE ops.name = 'Delivered' LIMIT 1;
        END IF;

        IF v_order.order_process_status_name = 'Completed' THEN
            v_status_timestamp := v_status_timestamp + INTERVAL '5 minutes';
            INSERT INTO order_status_history (
                id, order_id, order_process_status_id, note, changed_by,
                version, created_at, updated_at, is_deleted
            )
            SELECT
                gen_random_uuid(), v_order.id, ops.id, 'Order completed', 'system',
                0, v_status_timestamp, v_status_timestamp, false
            FROM order_process_statuses ops WHERE ops.name = 'Completed' LIMIT 1;
        END IF;

        IF v_order.order_process_status_name = 'Cancelled' THEN
            v_status_timestamp := v_status_timestamp + INTERVAL '2 minutes';
            INSERT INTO order_status_history (
                id, order_id, order_process_status_id, note, changed_by,
                version, created_at, updated_at, is_deleted
            )
            SELECT
                gen_random_uuid(), v_order.id, ops.id,
                CASE
                    WHEN RANDOM() < 0.5 THEN 'Cancelled by customer'
                    ELSE 'Cancelled by restaurant - out of stock'
                END,
                CASE
                    WHEN RANDOM() < 0.5 THEN 'customer'
                    ELSE 'business'
                END,
                0, v_status_timestamp, v_status_timestamp, false
            FROM order_process_statuses ops WHERE ops.name = 'Cancelled' LIMIT 1;
        END IF;

        -- ========================================================================
        -- 3. INSERT BUSINESS ORDER PAYMENT
        -- ========================================================================
        -- Only create payment records for non-cancelled orders
        IF v_order.order_process_status_name != 'Cancelled' THEN
            -- Generate unique payment reference
            v_payment_reference := 'PAY-' || TO_CHAR(v_order.created_at, 'YYYYMMDD') || '-' ||
                                  LPAD(FLOOR(RANDOM() * 999999)::TEXT, 6, '0');

            INSERT INTO business_order_payments (
                id,
                business_id,
                order_id,
                payment_reference,
                amount,
                payment_method,
                status,
                customer_payment_method,
                version,
                created_at,
                updated_at,
                is_deleted
            ) VALUES (
                gen_random_uuid(),
                v_order.business_id,
                v_order.id,
                v_payment_reference,
                v_order.total_amount,
                v_order.payment_method,
                v_order.payment_status,
                CASE v_order.payment_method
                    WHEN 'CASH' THEN 'Cash'
                    WHEN 'BANK_TRANSFER' THEN 'Bank Transfer'
                    WHEN 'ONLINE' THEN 'Online Payment'
                    WHEN 'OTHER' THEN 'Other'
                END,
                0,
                CASE
                    WHEN v_order.payment_status = 'COMPLETED' THEN v_order.completed_at
                    ELSE v_order.created_at
                END,
                CASE
                    WHEN v_order.payment_status = 'COMPLETED' THEN v_order.completed_at
                    ELSE v_order.created_at
                END,
                false
            );
        END IF;

    END LOOP;

    -- ========================================================================
    -- FINAL STATISTICS
    -- ========================================================================
    RAISE NOTICE '============================================================';
    RAISE NOTICE 'Order Related Data Population Complete!';
    RAISE NOTICE '============================================================';
    RAISE NOTICE 'Total Orders Processed: %', v_total_orders;
    RAISE NOTICE '';
    RAISE NOTICE 'Order Items:';
    RAISE NOTICE '  Total: %', (SELECT COUNT(*) FROM order_items WHERE NOT is_deleted);
    RAISE NOTICE '  With Promotions: %', (SELECT COUNT(*) FROM order_items WHERE has_promotion AND NOT is_deleted);
    RAISE NOTICE '  With Special Instructions: %', (SELECT COUNT(*) FROM order_items WHERE special_instructions IS NOT NULL AND NOT is_deleted);
    RAISE NOTICE '';
    RAISE NOTICE 'Order Status History:';
    RAISE NOTICE '  Total Records: %', (SELECT COUNT(*) FROM order_status_history WHERE NOT is_deleted);
    RAISE NOTICE '  Status Changes per Order (avg): %.1f',
        (SELECT AVG(cnt) FROM (
            SELECT COUNT(*) as cnt
            FROM order_status_history
            WHERE NOT is_deleted
            GROUP BY order_id
        ) sub);
    RAISE NOTICE '';
    RAISE NOTICE 'Business Order Payments:';
    RAISE NOTICE '  Total Payments: %', (SELECT COUNT(*) FROM business_order_payments WHERE NOT is_deleted);
    RAISE NOTICE '  COMPLETED: %', (SELECT COUNT(*) FROM business_order_payments WHERE status = 'COMPLETED' AND NOT is_deleted);
    RAISE NOTICE '  PENDING: %', (SELECT COUNT(*) FROM business_order_payments WHERE status = 'PENDING' AND NOT is_deleted);
    RAISE NOTICE '  FAILED: %', (SELECT COUNT(*) FROM business_order_payments WHERE status = 'FAILED' AND NOT is_deleted);
    RAISE NOTICE '  Total Amount: $%.2f', (SELECT COALESCE(SUM(amount), 0) FROM business_order_payments WHERE NOT is_deleted);
    RAISE NOTICE '============================================================';

END $$;
