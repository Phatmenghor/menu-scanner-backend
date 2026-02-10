-- Generate Order Statuses for Business
-- Business ID: 0a32d15e-1da6-4c39-bbe7-eec305035828

INSERT INTO order_process_statuses (id, business_id, name, description, color, "order", status, is_deleted, created_at, updated_at)
VALUES
    (gen_random_uuid(), '0a32d15e-1da6-4c39-bbe7-eec305035828', 'Pending', 'Order received, waiting for confirmation', '#FFA500', 1, 'ACTIVE', false, NOW(), NOW()),
    (gen_random_uuid(), '0a32d15e-1da6-4c39-bbe7-eec305035828', 'Confirmed', 'Order confirmed by restaurant', '#4169E1', 2, 'ACTIVE', false, NOW(), NOW()),
    (gen_random_uuid(), '0a32d15e-1da6-4c39-bbe7-eec305035828', 'Preparing', 'Food is being prepared', '#9370DB', 3, 'ACTIVE', false, NOW(), NOW()),
    (gen_random_uuid(), '0a32d15e-1da6-4c39-bbe7-eec305035828', 'Ready', 'Order is ready for pickup/delivery', '#32CD32', 4, 'ACTIVE', false, NOW(), NOW()),
    (gen_random_uuid(), '0a32d15e-1da6-4c39-bbe7-eec305035828', 'Out for Delivery', 'Order is on the way', '#1E90FF', 5, 'ACTIVE', false, NOW(), NOW()),
    (gen_random_uuid(), '0a32d15e-1da6-4c39-bbe7-eec305035828', 'Delivered', 'Order successfully delivered', '#228B22', 6, 'ACTIVE', false, NOW(), NOW()),
    (gen_random_uuid(), '0a32d15e-1da6-4c39-bbe7-eec305035828', 'Completed', 'Order completed', '#008000', 7, 'ACTIVE', false, NOW(), NOW()),
    (gen_random_uuid(), '0a32d15e-1da6-4c39-bbe7-eec305035828', 'Cancelled', 'Order cancelled', '#DC143C', 8, 'ACTIVE', false, NOW(), NOW())
ON CONFLICT DO NOTHING;

SELECT name, description, color FROM order_process_statuses
WHERE business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828'
ORDER BY "order";
