-- Migration script to add leave_type_enum_id column to leaves table
-- Run this script manually if the application still fails to start

-- Step 1: Add the column as nullable
ALTER TABLE leaves ADD COLUMN IF NOT EXISTS leave_type_enum_id UUID NULL;

-- Step 2: (Optional) Set a default value for existing records if needed
-- UPDATE leaves SET leave_type_enum_id = '<some-default-uuid>' WHERE leave_type_enum_id IS NULL;

-- Step 3: (Optional) Add index after all records have valid values
-- CREATE INDEX IF NOT EXISTS idx_leave_type ON leaves(leave_type_enum_id);

-- Step 4: (Optional) Make the column NOT NULL after all records have valid values
-- ALTER TABLE leaves ALTER COLUMN leave_type_enum_id SET NOT NULL;

-- Verify the column was added
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'leaves' AND column_name = 'leave_type_enum_id';
