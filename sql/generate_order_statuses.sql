-- Generate Order Statuses for Business
-- Business ID: 0a32d15e-1da6-4c39-bbe7-eec305035828

INSERT INTO order_process_statuses (id, business_id, name, description, status, is_deleted, created_at, updated_at)
VALUES
    (gen_random_uuid(), '0a32d15e-1da6-4c39-bbe7-eec305035828', 'Pending', 'Order received, waiting for confirmation', 'ACTIVE', false, NOW(), NOW()),
    (gen_random_uuid(), '0a32d15e-1da6-4c39-bbe7-eec305035828', 'Confirmed', 'Order confirmed by restaurant', 'ACTIVE', false, NOW(), NOW()),
    (gen_random_uuid(), '0a32d15e-1da6-4c39-bbe7-eec305035828', 'Preparing', 'Food is being prepared', 'ACTIVE', false, NOW(), NOW()),
    (gen_random_uuid(), '0a32d15e-1da6-4c39-bbe7-eec305035828', 'Ready', 'Order is ready for pickup/delivery', 'ACTIVE', false, NOW(), NOW()),
    (gen_random_uuid(), '0a32d15e-1da6-4c39-bbe7-eec305035828', 'Out for Delivery', 'Order is on the way', 'ACTIVE', false, NOW(), NOW()),
    (gen_random_uuid(), '0a32d15e-1da6-4c39-bbe7-eec305035828', 'Delivered', 'Order successfully delivered', 'ACTIVE', false, NOW(), NOW()),
    (gen_random_uuid(), '0a32d15e-1da6-4c39-bbe7-eec305035828', 'Completed', 'Order completed', 'ACTIVE', false, NOW(), NOW()),
    (gen_random_uuid(), '0a32d15e-1da6-4c39-bbe7-eec305035828', 'Cancelled', 'Order cancelled', 'ACTIVE', false, NOW(), NOW())
ON CONFLICT DO NOTHING;

SELECT name, description, status FROM order_process_statuses
WHERE business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828'
ORDER BY created_at;
