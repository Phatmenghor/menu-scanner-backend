-- Generate 80,000 Orders for Business
-- Business ID: 0a32d15e-1da6-4c39-bbe7-eec305035828
-- Date Range: 1 year before to 1 year after (2025-02-10 to 2027-02-10)

-- Step 1: Create temporary table with order statuses
CREATE TEMP TABLE temp_statuses AS
SELECT id, name FROM order_process_statuses
WHERE business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828'
ORDER BY created_at;

-- Step 2: Create helper arrays
DO $$
DECLARE
    product_names TEXT[] := ARRAY['Fried Rice', 'Pad Thai', 'Tom Yum Soup', 'Green Curry', 'Spring Rolls',
                                   'Beef Noodles', 'Chicken Satay', 'Mango Sticky Rice', 'Papaya Salad', 'BBQ Pork'];
    provinces TEXT[] := ARRAY['Phnom Penh', 'Siem Reap', 'Battambang', 'Kampong Cham', 'Kandal'];
    payment_methods TEXT[] := ARRAY['CASH', 'BANK_TRANSFER', 'ONLINE', 'OTHER'];

    v_order_id UUID;
    v_order_number TEXT;
    v_order_date TIMESTAMP;
    v_status_id UUID;
    v_status_name TEXT;
    v_subtotal NUMERIC(10,2);
    v_delivery_fee NUMERIC(10,2);
    v_item_count INT;
    v_product_name TEXT;
    v_base_price NUMERIC(10,2);
    v_final_price NUMERIC(10,2);
    v_quantity INT;
    v_has_discount BOOLEAN;
BEGIN
    -- Generate 80,000 orders
    FOR i IN 1..80000 LOOP
        v_order_id := gen_random_uuid();
        v_order_number := 'ORD-' || TO_CHAR(NOW(), 'YYYYMMDD') || '-' || LPAD(i::TEXT, 6, '0');

        -- Random date between 2025-02-10 and 2027-02-10
        v_order_date := TIMESTAMP '2025-02-10 00:00:00' +
                        (random() * (TIMESTAMP '2027-02-10 23:59:59' - TIMESTAMP '2025-02-10 00:00:00'));

        -- Random status
        SELECT id, name INTO v_status_id, v_status_name
        FROM temp_statuses
        ORDER BY random()
        LIMIT 1;

        -- Random delivery fee (70% have delivery)
        IF random() < 0.7 THEN
            v_delivery_fee := ROUND((1.0 + random() * 4.0)::NUMERIC, 2);
        ELSE
            v_delivery_fee := 0;
        END IF;

        -- Insert order
        INSERT INTO orders (
            id, order_number, business_id, customer_id,
            order_process_status_id, payment_method, is_paid,
            delivery_address_snapshot, delivery_option_name, delivery_option_description,
            delivery_fee, subtotal, total_amount,
            customer_note, confirmed_at, completed_at,
            is_deleted, version, created_at, updated_at
        ) VALUES (
            v_order_id,
            v_order_number,
            '0a32d15e-1da6-4c39-bbe7-eec305035828',
            NULL, -- no customer for test data
            v_status_id,
            payment_methods[1 + floor(random() * array_length(payment_methods, 1))::INT],
            random() < 0.5, -- 50% paid
            CASE
                WHEN v_delivery_fee > 0 THEN
                    provinces[1 + floor(random() * array_length(provinces, 1))::INT] || ' District, ' ||
                    provinces[1 + floor(random() * array_length(provinces, 1))::INT]
                ELSE NULL
            END,
            CASE WHEN v_delivery_fee > 0 THEN 'Standard Delivery' ELSE NULL END,
            CASE WHEN v_delivery_fee > 0 THEN '30-45 minutes' ELSE NULL END,
            v_delivery_fee,
            0, -- will be updated after items
            0, -- will be updated after items
            CASE WHEN random() < 0.3 THEN 'Please call when arriving' ELSE NULL END,
            CASE
                WHEN v_status_name IN ('Confirmed', 'Preparing', 'Ready', 'Delivered', 'Completed')
                THEN v_order_date + (5 + random() * 25) * INTERVAL '1 minute'
                ELSE NULL
            END,
            CASE
                WHEN v_status_name IN ('Delivered', 'Completed')
                THEN v_order_date + (30 + random() * 90) * INTERVAL '1 minute'
                ELSE NULL
            END,
            false,
            0, -- version
            v_order_date,
            v_order_date
        );

        -- Generate 1-5 items per order
        v_item_count := 1 + floor(random() * 5)::INT;
        v_subtotal := 0;

        FOR j IN 1..v_item_count LOOP
            v_product_name := product_names[1 + floor(random() * array_length(product_names, 1))::INT];
            v_base_price := ROUND((3.0 + random() * 12.0)::NUMERIC, 2);
            v_has_discount := random() < 0.3; -- 30% have discount
            v_quantity := 1 + floor(random() * 3)::INT;

            IF v_has_discount THEN
                v_final_price := ROUND((v_base_price * 0.8)::NUMERIC, 2); -- 20% discount
            ELSE
                v_final_price := v_base_price;
            END IF;

            INSERT INTO order_items (
                id, order_id, product_id, product_name, size_name,
                current_price, final_price, unit_price,
                has_promotion, promotion_type, promotion_value,
                quantity, total_price,
                is_deleted, version, created_at, updated_at
            ) VALUES (
                gen_random_uuid(),
                v_order_id,
                gen_random_uuid(),
                v_product_name,
                CASE WHEN random() < 0.5 THEN 'Regular' ELSE 'Large' END,
                v_base_price,
                v_final_price,
                v_final_price,
                v_has_discount,
                CASE WHEN v_has_discount THEN 'PERCENTAGE' ELSE NULL END,
                CASE WHEN v_has_discount THEN 20 ELSE NULL END,
                v_quantity,
                v_final_price * v_quantity,
                false,
                0, -- version
                v_order_date,
                v_order_date
            );

            v_subtotal := v_subtotal + (v_final_price * v_quantity);
        END LOOP;

        -- Update order totals
        UPDATE orders
        SET subtotal = v_subtotal,
            total_amount = v_subtotal + v_delivery_fee
        WHERE id = v_order_id;

        -- Log progress every 10,000 orders
        IF i % 10000 = 0 THEN
            RAISE NOTICE 'Generated % orders...', i;
        END IF;
    END LOOP;

    RAISE NOTICE 'Successfully generated 80,000 orders!';
END $$;

-- Verify results
SELECT
    COUNT(*) as total_orders,
    MIN(created_at) as earliest_order,
    MAX(created_at) as latest_order,
    ROUND(AVG(total_amount), 2) as avg_order_value,
    ROUND(SUM(total_amount), 2) as total_revenue
FROM orders
WHERE business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828';

SELECT
    ops.name as status,
    COUNT(*) as order_count
FROM orders o
JOIN order_process_statuses ops ON o.order_process_status_id = ops.id
WHERE o.business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828'
GROUP BY ops.name, ops."order"
ORDER BY ops."order";
