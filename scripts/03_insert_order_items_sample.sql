-- =====================================================
-- Insert Order Items for Sample Orders (Optional)
-- This creates 1-5 items per order for the first 100 orders
-- Business ID: 0a32d15e-1da6-4c39-bbe7-eec305035828
-- Customer ID: b00bde3f-5287-4eec-bfe5-1daa2b2a44ba
-- =====================================================

-- NOTE: This script requires that you have products in your database.
-- Replace the product_id values below with actual product IDs from your database.

-- First, let's get a sample product ID (you'll need to run this query first to get actual product IDs)
-- SELECT id, name FROM products WHERE business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828' LIMIT 10;

DO $$
DECLARE
    v_order_record RECORD;
    v_item_count INTEGER;
    v_product_ids UUID[] := ARRAY[
        -- REPLACE THESE WITH ACTUAL PRODUCT IDS FROM YOUR DATABASE
        gen_random_uuid(),
        gen_random_uuid(),
        gen_random_uuid(),
        gen_random_uuid(),
        gen_random_uuid()
    ];
    v_product_names VARCHAR[] := ARRAY['Burger', 'Pizza', 'Pasta', 'Salad', 'Fries'];
    v_counter INTEGER := 0;
    v_item_price DECIMAL(10,2);
    v_quantity INTEGER;
    v_total_price DECIMAL(10,2);
BEGIN
    -- Loop through first 100 orders
    FOR v_order_record IN
        SELECT id, order_number, subtotal
        FROM orders
        WHERE business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828'
          AND customer_id = 'b00bde3f-5287-4eec-bfe5-1daa2b2a44ba'
        ORDER BY created_at DESC
        LIMIT 100
    LOOP
        -- Random number of items (1-5)
        v_item_count := FLOOR(RANDOM() * 5 + 1)::INTEGER;

        -- Insert items for this order
        FOR i IN 1..v_item_count LOOP
            -- Random price between 3.00 and 25.00
            v_item_price := (RANDOM() * 22 + 3)::DECIMAL(10,2);

            -- Random quantity (1-3)
            v_quantity := FLOOR(RANDOM() * 3 + 1)::INTEGER;

            -- Calculate total
            v_total_price := v_item_price * v_quantity;

            INSERT INTO order_items (
                id,
                order_id,
                product_id,
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
                v_order_record.id,
                v_product_ids[FLOOR(RANDOM() * 5 + 1)],
                v_product_names[FLOOR(RANDOM() * 5 + 1)],
                'https://via.placeholder.com/150',
                'Standard',
                v_item_price,
                v_item_price,
                v_item_price,
                RANDOM() < 0.2, -- 20% chance of promotion
                CASE WHEN RANDOM() < 0.2 THEN 'PERCENTAGE' ELSE NULL END,
                CASE WHEN RANDOM() < 0.2 THEN 10.00 ELSE NULL END,
                v_quantity,
                v_total_price,
                CASE
                    WHEN RANDOM() < 0.3 THEN 'Extra sauce'
                    WHEN RANDOM() < 0.6 THEN 'No onions'
                    ELSE NULL
                END,
                0,
                NOW(),
                NOW(),
                false
            );
        END LOOP;

        v_counter := v_counter + 1;

        -- Print progress
        IF v_counter % 10 = 0 THEN
            RAISE NOTICE 'Inserted items for % orders...', v_counter;
        END IF;
    END LOOP;

    RAISE NOTICE 'Successfully inserted order items for % orders!', v_counter;
END $$;

-- Verify insertion
SELECT
    o.order_number,
    COUNT(oi.id) as item_count,
    SUM(oi.total_price) as items_total
FROM orders o
LEFT JOIN order_items oi ON o.id = oi.order_id
WHERE o.business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828'
  AND o.customer_id = 'b00bde3f-5287-4eec-bfe5-1daa2b2a44ba'
GROUP BY o.order_number
HAVING COUNT(oi.id) > 0
ORDER BY o.created_at DESC
LIMIT 20;
