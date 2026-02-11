/*
 * ============================================================================
 * Script: 01_insert_base_data.sql
 * Description: Creates foundational data needed for order generation
 *              - Order Process Statuses
 *              - Sample Products for the business
 * Business ID: 0a32d15e-1da6-4c39-bbe7-eec305035828
 * ============================================================================
 */

DO $$
DECLARE
    v_business_id UUID := '0a32d15e-1da6-4c39-bbe7-eec305035828';
    v_category_id UUID;
    v_product_count INTEGER := 0;
BEGIN
    RAISE NOTICE '============================================================';
    RAISE NOTICE 'Starting Base Data Population';
    RAISE NOTICE '============================================================';

    -- ========================================================================
    -- 1. INSERT ORDER PROCESS STATUSES
    -- ========================================================================
    RAISE NOTICE 'Creating order process statuses...';

    INSERT INTO order_process_statuses (
        id,
        business_id,
        name,
        description,
        display_order,
        version,
        created_at,
        updated_at,
        is_deleted
    ) VALUES
        (gen_random_uuid(), v_business_id, 'Pending', 'Order placed, waiting for confirmation', 1, 0, NOW(), NOW(), false),
        (gen_random_uuid(), v_business_id, 'Confirmed', 'Order confirmed by restaurant', 2, 0, NOW(), NOW(), false),
        (gen_random_uuid(), v_business_id, 'Preparing', 'Order being prepared in kitchen', 3, 0, NOW(), NOW(), false),
        (gen_random_uuid(), v_business_id, 'Ready', 'Order ready for pickup/delivery', 4, 0, NOW(), NOW(), false),
        (gen_random_uuid(), v_business_id, 'Out for Delivery', 'Order is out for delivery', 5, 0, NOW(), NOW(), false),
        (gen_random_uuid(), v_business_id, 'Delivered', 'Order delivered to customer', 6, 0, NOW(), NOW(), false),
        (gen_random_uuid(), v_business_id, 'Completed', 'Order completed successfully', 7, 0, NOW(), NOW(), false),
        (gen_random_uuid(), v_business_id, 'Cancelled', 'Order cancelled', 8, 0, NOW(), NOW(), false)
    ON CONFLICT (business_id, name) DO NOTHING;

    RAISE NOTICE '✓ Order process statuses created';

    -- ========================================================================
    -- 2. INSERT SAMPLE PRODUCTS
    -- ========================================================================
    RAISE NOTICE 'Creating sample products...';

    -- Main Dishes
    INSERT INTO products (
        id, business_id, name, description, price, image_url,
        is_available, is_deleted, version, created_at, updated_at
    ) VALUES
        (gen_random_uuid(), v_business_id, 'Classic Burger', 'Juicy beef patty with lettuce, tomato, and special sauce', 8.99, 'https://via.placeholder.com/300/burger', true, false, 0, NOW(), NOW()),
        (gen_random_uuid(), v_business_id, 'Cheeseburger', 'Classic burger with melted cheddar cheese', 9.99, 'https://via.placeholder.com/300/cheeseburger', true, false, 0, NOW(), NOW()),
        (gen_random_uuid(), v_business_id, 'BBQ Bacon Burger', 'Topped with crispy bacon and BBQ sauce', 11.99, 'https://via.placeholder.com/300/bbqburger', true, false, 0, NOW(), NOW()),
        (gen_random_uuid(), v_business_id, 'Margherita Pizza', 'Fresh mozzarella, basil, and tomato sauce', 12.99, 'https://via.placeholder.com/300/margherita', true, false, 0, NOW(), NOW()),
        (gen_random_uuid(), v_business_id, 'Pepperoni Pizza', 'Classic pepperoni with mozzarella', 14.99, 'https://via.placeholder.com/300/pepperoni', true, false, 0, NOW(), NOW()),
        (gen_random_uuid(), v_business_id, 'Supreme Pizza', 'Loaded with pepperoni, sausage, peppers, and onions', 16.99, 'https://via.placeholder.com/300/supreme', true, false, 0, NOW(), NOW()),
        (gen_random_uuid(), v_business_id, 'Spaghetti Carbonara', 'Creamy pasta with bacon and parmesan', 13.99, 'https://via.placeholder.com/300/carbonara', true, false, 0, NOW(), NOW()),
        (gen_random_uuid(), v_business_id, 'Fettuccine Alfredo', 'Rich and creamy alfredo sauce', 12.99, 'https://via.placeholder.com/300/alfredo', true, false, 0, NOW(), NOW()),
        (gen_random_uuid(), v_business_id, 'Penne Arrabbiata', 'Spicy tomato sauce with garlic', 11.99, 'https://via.placeholder.com/300/arrabbiata', true, false, 0, NOW(), NOW()),
        (gen_random_uuid(), v_business_id, 'Grilled Chicken Breast', 'Seasoned grilled chicken with herbs', 14.99, 'https://via.placeholder.com/300/chicken', true, false, 0, NOW(), NOW()),
        (gen_random_uuid(), v_business_id, 'Beef Steak', 'Premium cut grilled to perfection', 24.99, 'https://via.placeholder.com/300/steak', true, false, 0, NOW(), NOW()),
        (gen_random_uuid(), v_business_id, 'Fish and Chips', 'Crispy battered fish with fries', 15.99, 'https://via.placeholder.com/300/fishandchips', true, false, 0, NOW(), NOW()),
        (gen_random_uuid(), v_business_id, 'Pad Thai', 'Thai stir-fried noodles with peanuts', 13.99, 'https://via.placeholder.com/300/padthai', true, false, 0, NOW(), NOW()),
        (gen_random_uuid(), v_business_id, 'Fried Rice', 'Wok-fried rice with vegetables and egg', 9.99, 'https://via.placeholder.com/300/friedrice', true, false, 0, NOW(), NOW()),
        (gen_random_uuid(), v_business_id, 'Chicken Curry', 'Spicy curry with coconut milk', 14.99, 'https://via.placeholder.com/300/curry', true, false, 0, NOW(), NOW());

    v_product_count := v_product_count + 15;

    -- Sides
    INSERT INTO products (
        id, business_id, name, description, price, image_url,
        is_available, is_deleted, version, created_at, updated_at
    ) VALUES
        (gen_random_uuid(), v_business_id, 'French Fries', 'Crispy golden fries', 4.99, 'https://via.placeholder.com/300/fries', true, false, 0, NOW(), NOW()),
        (gen_random_uuid(), v_business_id, 'Onion Rings', 'Crispy battered onion rings', 5.99, 'https://via.placeholder.com/300/onionrings', true, false, 0, NOW(), NOW()),
        (gen_random_uuid(), v_business_id, 'Caesar Salad', 'Fresh romaine with caesar dressing', 6.99, 'https://via.placeholder.com/300/caesar', true, false, 0, NOW(), NOW()),
        (gen_random_uuid(), v_business_id, 'Garden Salad', 'Mixed greens with vegetables', 5.99, 'https://via.placeholder.com/300/garden', true, false, 0, NOW(), NOW()),
        (gen_random_uuid(), v_business_id, 'Garlic Bread', 'Toasted bread with garlic butter', 3.99, 'https://via.placeholder.com/300/garlicbread', true, false, 0, NOW(), NOW()),
        (gen_random_uuid(), v_business_id, 'Mozzarella Sticks', 'Fried mozzarella with marinara', 6.99, 'https://via.placeholder.com/300/mozzarella', true, false, 0, NOW(), NOW()),
        (gen_random_uuid(), v_business_id, 'Buffalo Wings', 'Spicy chicken wings', 8.99, 'https://via.placeholder.com/300/wings', true, false, 0, NOW(), NOW()),
        (gen_random_uuid(), v_business_id, 'Nachos', 'Tortilla chips with cheese and jalapeños', 7.99, 'https://via.placeholder.com/300/nachos', true, false, 0, NOW(), NOW());

    v_product_count := v_product_count + 8;

    -- Drinks
    INSERT INTO products (
        id, business_id, name, description, price, image_url,
        is_available, is_deleted, version, created_at, updated_at
    ) VALUES
        (gen_random_uuid(), v_business_id, 'Coca-Cola', 'Classic Coke', 2.50, 'https://via.placeholder.com/300/coke', true, false, 0, NOW(), NOW()),
        (gen_random_uuid(), v_business_id, 'Pepsi', 'Pepsi Cola', 2.50, 'https://via.placeholder.com/300/pepsi', true, false, 0, NOW(), NOW()),
        (gen_random_uuid(), v_business_id, 'Sprite', 'Lemon-lime soda', 2.50, 'https://via.placeholder.com/300/sprite', true, false, 0, NOW(), NOW()),
        (gen_random_uuid(), v_business_id, 'Orange Juice', 'Freshly squeezed', 3.99, 'https://via.placeholder.com/300/oj', true, false, 0, NOW(), NOW()),
        (gen_random_uuid(), v_business_id, 'Iced Tea', 'Sweetened iced tea', 2.99, 'https://via.placeholder.com/300/icedtea', true, false, 0, NOW(), NOW()),
        (gen_random_uuid(), v_business_id, 'Coffee', 'Fresh brewed coffee', 2.99, 'https://via.placeholder.com/300/coffee', true, false, 0, NOW(), NOW()),
        (gen_random_uuid(), v_business_id, 'Latte', 'Espresso with steamed milk', 4.50, 'https://via.placeholder.com/300/latte', true, false, 0, NOW(), NOW()),
        (gen_random_uuid(), v_business_id, 'Cappuccino', 'Espresso with foamed milk', 4.50, 'https://via.placeholder.com/300/cappuccino', true, false, 0, NOW(), NOW());

    v_product_count := v_product_count + 8;

    -- Desserts
    INSERT INTO products (
        id, business_id, name, description, price, image_url,
        is_available, is_deleted, version, created_at, updated_at
    ) VALUES
        (gen_random_uuid(), v_business_id, 'Chocolate Cake', 'Rich chocolate layer cake', 6.99, 'https://via.placeholder.com/300/choccake', true, false, 0, NOW(), NOW()),
        (gen_random_uuid(), v_business_id, 'Cheesecake', 'New York style cheesecake', 7.99, 'https://via.placeholder.com/300/cheesecake', true, false, 0, NOW(), NOW()),
        (gen_random_uuid(), v_business_id, 'Ice Cream Sundae', 'Vanilla ice cream with toppings', 5.99, 'https://via.placeholder.com/300/sundae', true, false, 0, NOW(), NOW()),
        (gen_random_uuid(), v_business_id, 'Apple Pie', 'Warm apple pie with cinnamon', 6.99, 'https://via.placeholder.com/300/applepie', true, false, 0, NOW(), NOW()),
        (gen_random_uuid(), v_business_id, 'Brownie', 'Fudgy chocolate brownie', 4.99, 'https://via.placeholder.com/300/brownie', true, false, 0, NOW(), NOW());

    v_product_count := v_product_count + 5;

    RAISE NOTICE '✓ Created % sample products', v_product_count;

    -- ========================================================================
    -- FINAL STATISTICS
    -- ========================================================================
    RAISE NOTICE '';
    RAISE NOTICE '============================================================';
    RAISE NOTICE 'Base Data Population Complete!';
    RAISE NOTICE '============================================================';
    RAISE NOTICE 'Order Process Statuses: %', (SELECT COUNT(*) FROM order_process_statuses WHERE business_id = v_business_id AND NOT is_deleted);
    RAISE NOTICE 'Products Created: %', (SELECT COUNT(*) FROM products WHERE business_id = v_business_id AND NOT is_deleted);
    RAISE NOTICE '  - Main Dishes: 15';
    RAISE NOTICE '  - Sides: 8';
    RAISE NOTICE '  - Drinks: 8';
    RAISE NOTICE '  - Desserts: 5';
    RAISE NOTICE '============================================================';

END $$;
