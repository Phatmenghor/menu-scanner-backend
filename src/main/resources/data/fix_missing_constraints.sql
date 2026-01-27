-- ============================================================
-- FIX MISSING PRIMARY KEY AND UNIQUE CONSTRAINTS
-- Run this in pgAdmin against the e_menu_platform database
-- ============================================================

-- ============================================================
-- PART 1: ADD MISSING PRIMARY KEYS ON ALL ENTITY TABLES
-- ============================================================

-- Clean up any NULL id rows first (shouldn't exist, but just in case)
DO $$
DECLARE
    tbl TEXT;
    dup_count INTEGER;
    tables TEXT[] := ARRAY[
        'audit_logs', 'blacklisted_tokens', 'businesses', 'business_settings',
        'refresh_tokens', 'roles', 'users', 'user_sessions',
        'attendances', 'attendance_check_ins', 'leaves', 'work_schedules',
        'location_commune_cbc', 'customer_addresses', 'location_district_cbc',
        'location_province_cbc', 'location_village_cbc',
        'banners', 'brands', 'categories',
        'product_favorites', 'product_images', 'product_sizes', 'products',
        'notifications',
        'business_exchange_rates', 'business_order_payments',
        'carts', 'cart_items', 'delivery_options', 'exchange_rates',
        'order_items', 'order_status_history', 'orders', 'payments',
        'images', 'leave_type_enum', 'work_schedule_type_enum',
        'subscriptions', 'subscription_plans'
    ];
    has_pk BOOLEAN;
    tbl_exists BOOLEAN;
BEGIN
    FOREACH tbl IN ARRAY tables LOOP
        -- Check if table exists
        SELECT EXISTS (
            SELECT 1 FROM information_schema.tables
            WHERE table_schema = 'public' AND table_name = tbl
        ) INTO tbl_exists;

        IF NOT tbl_exists THEN
            RAISE NOTICE 'Table % does not exist, skipping...', tbl;
            CONTINUE;
        END IF;

        -- Check if primary key already exists
        SELECT EXISTS (
            SELECT 1 FROM information_schema.table_constraints
            WHERE table_schema = 'public' AND table_name = tbl AND constraint_type = 'PRIMARY KEY'
        ) INTO has_pk;

        IF has_pk THEN
            RAISE NOTICE 'Table % already has PRIMARY KEY, skipping...', tbl;
        ELSE
            -- Remove rows with NULL id
            EXECUTE format('DELETE FROM %I WHERE id IS NULL', tbl);

            -- Remove duplicate id rows, keeping only the one with the latest ctid
            EXECUTE format(
                'DELETE FROM %I a USING %I b
                 WHERE a.id = b.id AND a.ctid < b.ctid',
                tbl, tbl
            );
            GET DIAGNOSTICS dup_count = ROW_COUNT;
            IF dup_count > 0 THEN
                RAISE NOTICE 'Removed % duplicate row(s) from table: %', dup_count, tbl;
            END IF;

            -- Add primary key
            EXECUTE format('ALTER TABLE %I ADD PRIMARY KEY (id)', tbl);
            RAISE NOTICE 'Added PRIMARY KEY to table: %', tbl;
        END IF;
    END LOOP;
END $$;


-- ============================================================
-- PART 2: ADD MISSING UNIQUE CONSTRAINTS ON LOCATION TABLES
-- ============================================================

DO $$
DECLARE
    has_constraint BOOLEAN;
BEGIN
    -- Province: uk_province_code
    SELECT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE table_schema = 'public' AND table_name = 'location_province_cbc' AND constraint_name = 'uk_province_code'
    ) INTO has_constraint;
    IF NOT has_constraint THEN
        ALTER TABLE location_province_cbc ADD CONSTRAINT uk_province_code UNIQUE (province_code);
        RAISE NOTICE 'Added UNIQUE constraint uk_province_code to location_province_cbc';
    ELSE
        RAISE NOTICE 'Constraint uk_province_code already exists, skipping...';
    END IF;

    -- District: uk_district_code
    SELECT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE table_schema = 'public' AND table_name = 'location_district_cbc' AND constraint_name = 'uk_district_code'
    ) INTO has_constraint;
    IF NOT has_constraint THEN
        ALTER TABLE location_district_cbc ADD CONSTRAINT uk_district_code UNIQUE (district_code);
        RAISE NOTICE 'Added UNIQUE constraint uk_district_code to location_district_cbc';
    ELSE
        RAISE NOTICE 'Constraint uk_district_code already exists, skipping...';
    END IF;

    -- Commune: uk_commune_code
    SELECT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE table_schema = 'public' AND table_name = 'location_commune_cbc' AND constraint_name = 'uk_commune_code'
    ) INTO has_constraint;
    IF NOT has_constraint THEN
        ALTER TABLE location_commune_cbc ADD CONSTRAINT uk_commune_code UNIQUE (commune_code);
        RAISE NOTICE 'Added UNIQUE constraint uk_commune_code to location_commune_cbc';
    ELSE
        RAISE NOTICE 'Constraint uk_commune_code already exists, skipping...';
    END IF;

    -- Village: uk_village_code
    SELECT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE table_schema = 'public' AND table_name = 'location_village_cbc' AND constraint_name = 'uk_village_code'
    ) INTO has_constraint;
    IF NOT has_constraint THEN
        ALTER TABLE location_village_cbc ADD CONSTRAINT uk_village_code UNIQUE (village_code);
        RAISE NOTICE 'Added UNIQUE constraint uk_village_code to location_village_cbc';
    ELSE
        RAISE NOTICE 'Constraint uk_village_code already exists, skipping...';
    END IF;
END $$;

-- ============================================================
-- DONE! Check the Messages tab in pgAdmin for NOTICE output.
-- ============================================================
