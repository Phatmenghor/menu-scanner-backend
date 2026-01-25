-- =====================================================
-- DATABASE SYNC SCRIPT FOR EMENU PLATFORM
-- Run this script to ensure your database matches the current entity models
-- =====================================================

-- ==========================
-- 1. DELIVERY_OPTIONS TABLE
-- ==========================

-- Create the delivery_options table if it doesn't exist
CREATE TABLE IF NOT EXISTS delivery_options (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255),
    business_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE'
);

-- Add foreign key constraint if not exists (safe to run multiple times)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_delivery_options_business'
        AND table_name = 'delivery_options'
    ) THEN
        ALTER TABLE delivery_options
        ADD CONSTRAINT fk_delivery_options_business
        FOREIGN KEY (business_id) REFERENCES businesses(id);
    END IF;
END $$;

-- Add any missing columns (safe to run multiple times)
DO $$
BEGIN
    -- Add version column if missing
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'delivery_options' AND column_name = 'version') THEN
        ALTER TABLE delivery_options ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
    END IF;

    -- Add created_at column if missing
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'delivery_options' AND column_name = 'created_at') THEN
        ALTER TABLE delivery_options ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;

    -- Add updated_at column if missing
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'delivery_options' AND column_name = 'updated_at') THEN
        ALTER TABLE delivery_options ADD COLUMN updated_at TIMESTAMP;
    END IF;

    -- Add created_by column if missing
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'delivery_options' AND column_name = 'created_by') THEN
        ALTER TABLE delivery_options ADD COLUMN created_by VARCHAR(255);
    END IF;

    -- Add updated_by column if missing
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'delivery_options' AND column_name = 'updated_by') THEN
        ALTER TABLE delivery_options ADD COLUMN updated_by VARCHAR(255);
    END IF;

    -- Add is_deleted column if missing
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'delivery_options' AND column_name = 'is_deleted') THEN
        ALTER TABLE delivery_options ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;

    -- Add deleted_at column if missing
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'delivery_options' AND column_name = 'deleted_at') THEN
        ALTER TABLE delivery_options ADD COLUMN deleted_at TIMESTAMP;
    END IF;

    -- Add deleted_by column if missing
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'delivery_options' AND column_name = 'deleted_by') THEN
        ALTER TABLE delivery_options ADD COLUMN deleted_by VARCHAR(255);
    END IF;

    -- Add image_url column if missing
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'delivery_options' AND column_name = 'image_url') THEN
        ALTER TABLE delivery_options ADD COLUMN image_url VARCHAR(500);
    END IF;

    -- Add description column if missing
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'delivery_options' AND column_name = 'description') THEN
        ALTER TABLE delivery_options ADD COLUMN description TEXT;
    END IF;

    -- Add status column if missing
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'delivery_options' AND column_name = 'status') THEN
        ALTER TABLE delivery_options ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE';
    END IF;
END $$;

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_delivery_options_business_id ON delivery_options(business_id);
CREATE INDEX IF NOT EXISTS idx_delivery_options_status ON delivery_options(status);
CREATE INDEX IF NOT EXISTS idx_delivery_options_is_deleted ON delivery_options(is_deleted);
CREATE INDEX IF NOT EXISTS idx_delivery_options_name ON delivery_options(name);

-- ==========================
-- VERIFICATION QUERIES
-- ==========================

-- Verify the table structure
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'delivery_options'
ORDER BY ordinal_position;

-- Verify indexes
SELECT indexname, indexdef
FROM pg_indexes
WHERE tablename = 'delivery_options';

-- Verify foreign keys
SELECT
    tc.constraint_name,
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
    AND tc.table_name = 'delivery_options';

-- ==========================
-- SUCCESS MESSAGE
-- ==========================
DO $$
BEGIN
    RAISE NOTICE '===========================================';
    RAISE NOTICE 'DATABASE SYNC COMPLETED SUCCESSFULLY!';
    RAISE NOTICE 'delivery_options table is now in sync.';
    RAISE NOTICE '===========================================';
END $$;
