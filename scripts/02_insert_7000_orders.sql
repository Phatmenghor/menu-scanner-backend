-- =====================================================
-- Insert 7000 Orders for Business and Customer
-- Business ID: 0a32d15e-1da6-4c39-bbe7-eec305035828
-- Customer ID: b00bde3f-5287-4eec-bfe5-1daa2b2a44ba
-- =====================================================

-- Create a temporary function to generate random decimal between min and max
DO $$
DECLARE
    v_business_id UUID := '0a32d15e-1da6-4c39-bbe7-eec305035828';
    v_customer_id UUID := 'b00bde3f-5287-4eec-bfe5-1daa2b2a44ba';
    v_counter INTEGER := 1;
    v_order_id UUID;
    v_order_number VARCHAR;
    v_subtotal DECIMAL(10,2);
    v_delivery_fee DECIMAL(10,2);
    v_total_amount DECIMAL(10,2);
    v_payment_method VARCHAR;
    v_payment_status VARCHAR;
    v_status_name VARCHAR;
    v_created_date TIMESTAMP;
    v_payment_methods VARCHAR[] := ARRAY['CASH', 'BANK_TRANSFER', 'ONLINE', 'OTHER'];
    v_payment_statuses VARCHAR[] := ARRAY['PAID', 'UNPAID', 'PARTIALLY_PAID'];
    v_order_statuses VARCHAR[] := ARRAY['Pending', 'Confirmed', 'Preparing', 'Ready', 'Out for Delivery', 'Delivered', 'Completed', 'Cancelled'];
BEGIN
    -- Loop to insert 7000 orders
    FOR v_counter IN 1..7000 LOOP
        -- Generate order ID and number
        v_order_id := gen_random_uuid();
        v_order_number := 'ORD-' || TO_CHAR(NOW(), 'YYYYMMDD') || '-' || LPAD(v_counter::TEXT, 6, '0');

        -- Random pricing (subtotal between 5.00 and 100.00)
        v_subtotal := (RANDOM() * 95 + 5)::DECIMAL(10,2);

        -- Random delivery fee (0, 2, 3, or 5)
        v_delivery_fee := (ARRAY[0, 2.00, 3.00, 5.00])[FLOOR(RANDOM() * 4 + 1)]::DECIMAL(10,2);

        -- Total amount
        v_total_amount := v_subtotal + v_delivery_fee;

        -- Random payment method
        v_payment_method := v_payment_methods[FLOOR(RANDOM() * 4 + 1)];

        -- Random payment status (80% PAID, 15% UNPAID, 5% PARTIALLY_PAID)
        CASE
            WHEN RANDOM() < 0.80 THEN v_payment_status := 'PAID';
            WHEN RANDOM() < 0.95 THEN v_payment_status := 'UNPAID';
            ELSE v_payment_status := 'PARTIALLY_PAID';
        END CASE;

        -- Random order status
        v_status_name := v_order_statuses[FLOOR(RANDOM() * 8 + 1)];

        -- Random created date (last 90 days)
        v_created_date := NOW() - (RANDOM() * INTERVAL '90 days');

        -- Insert order
        INSERT INTO orders (
            id,
            order_number,
            customer_id,
            business_id,
            delivery_address_snapshot,
            delivery_option_name,
            delivery_option_description,
            order_process_status_name,
            customer_note,
            business_note,
            subtotal,
            delivery_fee,
            total_amount,
            payment_method,
            payment_status,
            confirmed_at,
            completed_at,
            version,
            created_at,
            updated_at,
            is_deleted
        ) VALUES (
            v_order_id,
            v_order_number,
            v_customer_id,
            v_business_id,
            '{"street": "Street ' || v_counter || '", "city": "Phnom Penh", "district": "Chamkarmon", "commune": "Tonle Bassac", "postalCode": "12000", "country": "Cambodia"}',
            CASE
                WHEN v_delivery_fee = 0 THEN 'Pickup'
                ELSE 'Standard Delivery'
            END,
            CASE
                WHEN v_delivery_fee = 0 THEN 'Self pickup at restaurant'
                WHEN v_delivery_fee = 2 THEN 'Delivery within 2km'
                WHEN v_delivery_fee = 3 THEN 'Delivery within 5km'
                ELSE 'Delivery within 10km'
            END,
            v_status_name,
            CASE
                WHEN RANDOM() < 0.3 THEN 'Please add extra sauce'
                WHEN RANDOM() < 0.6 THEN 'No spicy please'
                ELSE NULL
            END,
            CASE
                WHEN RANDOM() < 0.2 THEN 'Priority customer'
                ELSE NULL
            END,
            v_subtotal,
            v_delivery_fee,
            v_total_amount,
            v_payment_method,
            v_payment_status,
            CASE
                WHEN v_status_name IN ('Confirmed', 'Preparing', 'Ready', 'Out for Delivery', 'Delivered', 'Completed')
                THEN v_created_date + INTERVAL '5 minutes'
                ELSE NULL
            END,
            CASE
                WHEN v_status_name = 'Completed'
                THEN v_created_date + INTERVAL '1 hour'
                ELSE NULL
            END,
            0,
            v_created_date,
            v_created_date,
            false
        );

        -- Print progress every 1000 records
        IF v_counter % 1000 = 0 THEN
            RAISE NOTICE 'Inserted % orders...', v_counter;
        END IF;
    END LOOP;

    RAISE NOTICE 'Successfully inserted 7000 orders!';
END $$;

-- Verify insertion
SELECT
    COUNT(*) as total_orders,
    payment_status,
    order_process_status_name
FROM orders
WHERE business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828'
  AND customer_id = 'b00bde3f-5287-4eec-bfe5-1daa2b2a44ba'
GROUP BY payment_status, order_process_status_name
ORDER BY order_process_status_name, payment_status;

-- Check total count
SELECT COUNT(*) as total_count
FROM orders
WHERE business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828'
  AND customer_id = 'b00bde3f-5287-4eec-bfe5-1daa2b2a44ba';

-- Sample of created orders
SELECT
    order_number,
    order_process_status_name,
    payment_method,
    payment_status,
    total_amount,
    created_at
FROM orders
WHERE business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828'
  AND customer_id = 'b00bde3f-5287-4eec-bfe5-1daa2b2a44ba'
ORDER BY created_at DESC
LIMIT 20;
