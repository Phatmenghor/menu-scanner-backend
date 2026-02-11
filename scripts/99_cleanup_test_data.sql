-- =====================================================
-- Clean Up Test Data (USE WITH CAUTION!)
-- Business ID: 0a32d15e-1da6-4c39-bbe7-eec305035828
-- Customer ID: b00bde3f-5287-4eec-bfe5-1daa2b2a44ba
-- =====================================================

-- ⚠️ WARNING: This will DELETE all orders and order items for the specified business and customer
-- Make sure you have a backup before running this script!

DO $$
DECLARE
    v_business_id UUID := '0a32d15e-1da6-4c39-bbe7-eec305035828';
    v_customer_id UUID := 'b00bde3f-5287-4eec-bfe5-1daa2b2a44ba';
    v_order_items_deleted INTEGER;
    v_orders_deleted INTEGER;
    v_statuses_deleted INTEGER;
BEGIN
    -- Count before deletion
    RAISE NOTICE 'Starting cleanup process...';

    -- Delete order items first (due to foreign key constraint)
    DELETE FROM order_items
    WHERE order_id IN (
        SELECT id FROM orders
        WHERE business_id = v_business_id
          AND customer_id = v_customer_id
    );
    GET DIAGNOSTICS v_order_items_deleted = ROW_COUNT;
    RAISE NOTICE 'Deleted % order items', v_order_items_deleted;

    -- Delete orders
    DELETE FROM orders
    WHERE business_id = v_business_id
      AND customer_id = v_customer_id;
    GET DIAGNOSTICS v_orders_deleted = ROW_COUNT;
    RAISE NOTICE 'Deleted % orders', v_orders_deleted;

    -- Optionally delete order process statuses (uncomment if needed)
    -- DELETE FROM order_process_statuses
    -- WHERE business_id = v_business_id;
    -- GET DIAGNOSTICS v_statuses_deleted = ROW_COUNT;
    -- RAISE NOTICE 'Deleted % order process statuses', v_statuses_deleted;

    RAISE NOTICE 'Cleanup completed successfully!';
    RAISE NOTICE 'Summary: Deleted % orders and % order items', v_orders_deleted, v_order_items_deleted;
END $$;

-- Verify deletion
SELECT
    'Orders' as table_name,
    COUNT(*) as remaining_records
FROM orders
WHERE business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828'
  AND customer_id = 'b00bde3f-5287-4eec-bfe5-1daa2b2a44ba'
UNION ALL
SELECT
    'Order Items' as table_name,
    COUNT(*) as remaining_records
FROM order_items
WHERE order_id IN (
    SELECT id FROM orders
    WHERE business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828'
      AND customer_id = 'b00bde3f-5287-4eec-bfe5-1daa2b2a44ba'
);
