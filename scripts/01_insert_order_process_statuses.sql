-- =====================================================
-- Insert Order Process Statuses for Business
-- Business ID: 0a32d15e-1da6-4c39-bbe7-eec305035828
-- =====================================================

INSERT INTO order_process_statuses (
    id,
    business_id,
    name,
    description,
    status,
    version,
    created_at,
    updated_at,
    is_deleted
) VALUES
-- Pending Status
(
    gen_random_uuid(),
    '0a32d15e-1da6-4c39-bbe7-eec305035828',
    'Pending',
    'Order has been placed and waiting for confirmation',
    'ACTIVE',
    0,
    NOW(),
    NOW(),
    false
),
-- Confirmed Status
(
    gen_random_uuid(),
    '0a32d15e-1da6-4c39-bbe7-eec305035828',
    'Confirmed',
    'Order has been confirmed by the restaurant',
    'ACTIVE',
    0,
    NOW(),
    NOW(),
    false
),
-- Preparing Status
(
    gen_random_uuid(),
    '0a32d15e-1da6-4c39-bbe7-eec305035828',
    'Preparing',
    'Order is being prepared in the kitchen',
    'ACTIVE',
    0,
    NOW(),
    NOW(),
    false
),
-- Ready Status
(
    gen_random_uuid(),
    '0a32d15e-1da6-4c39-bbe7-eec305035828',
    'Ready',
    'Order is ready for pickup or delivery',
    'ACTIVE',
    0,
    NOW(),
    NOW(),
    false
),
-- Out for Delivery Status
(
    gen_random_uuid(),
    '0a32d15e-1da6-4c39-bbe7-eec305035828',
    'Out for Delivery',
    'Order is out for delivery',
    'ACTIVE',
    0,
    NOW(),
    NOW(),
    false
),
-- Delivered Status
(
    gen_random_uuid(),
    '0a32d15e-1da6-4c39-bbe7-eec305035828',
    'Delivered',
    'Order has been delivered to customer',
    'ACTIVE',
    0,
    NOW(),
    NOW(),
    false
),
-- Completed Status
(
    gen_random_uuid(),
    '0a32d15e-1da6-4c39-bbe7-eec305035828',
    'Completed',
    'Order has been completed successfully',
    'ACTIVE',
    0,
    NOW(),
    NOW(),
    false
),
-- Cancelled Status
(
    gen_random_uuid(),
    '0a32d15e-1da6-4c39-bbe7-eec305035828',
    'Cancelled',
    'Order has been cancelled',
    'ACTIVE',
    0,
    NOW(),
    NOW(),
    false
)
ON CONFLICT (business_id, name) DO NOTHING;

-- Verify insertion
SELECT id, name, description, status
FROM order_process_statuses
WHERE business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828'
ORDER BY name;
