# Database Migration Guide: Remove OrderStatus Enum

## Overview
This migration removes the hardcoded `status` column from `orders` and `order_status_history` tables, as we now use the dynamic `order_process_status_id` approach.

## Prerequisites
- Backup your database before running this migration
- Ensure all applications using the old `status` column are stopped or updated

## Migration Steps

### 1. Apply the Migration

Run the migration script to remove the `status` column:

```bash
psql -U your_username -d your_database -f migration-remove-status-column.sql
```

Or manually execute:

```sql
-- Make status nullable first
ALTER TABLE orders ALTER COLUMN status DROP NOT NULL;
ALTER TABLE order_status_history ALTER COLUMN status DROP NOT NULL;

-- Drop the status columns
ALTER TABLE orders DROP COLUMN IF EXISTS status;
ALTER TABLE order_status_history DROP COLUMN IF EXISTS status;

-- Ensure order_process_status_id is required
ALTER TABLE orders ALTER COLUMN order_process_status_id SET NOT NULL;
ALTER TABLE order_status_history ALTER COLUMN order_process_status_id SET NOT NULL;
```

### 2. Verify the Migration

Check that the columns were removed:

```sql
-- Check orders table
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'orders' AND column_name IN ('status', 'order_process_status_id');

-- Check order_status_history table
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'order_status_history' AND column_name IN ('status', 'order_process_status_id');
```

Expected results:
- `status` column should NOT appear
- `order_process_status_id` should have `is_nullable = 'NO'`

### 3. Run the Seed Script

After migration, you can run the fixed seed script:

```bash
psql -U your_username -d your_database -f seed-orders-fixed.sql
```

## Rollback (If Needed)

If you need to rollback this migration, create the migration script below and run it:

```sql
-- Rollback: Add status column back

-- Add status column to orders table
ALTER TABLE orders ADD COLUMN status VARCHAR(50);

-- Add status column to order_status_history table
ALTER TABLE order_status_history ADD COLUMN status VARCHAR(50);

-- Populate status based on order_process_status.status_type
UPDATE orders o
SET status = ops.status_type
FROM order_process_statuses ops
WHERE o.order_process_status_id = ops.id;

UPDATE order_status_history osh
SET status = ops.status_type
FROM order_process_statuses ops
WHERE osh.order_process_status_id = ops.id;

-- Make status NOT NULL after populating
ALTER TABLE orders ALTER COLUMN status SET NOT NULL;
ALTER TABLE order_status_history ALTER COLUMN status SET NOT NULL;
```

## Important Notes

1. **Application Compatibility**: Ensure your application code (Java entities) matches the database schema
2. **Data Integrity**: The migration maintains data integrity by using `order_process_status_id`
3. **No Data Loss**: This migration only removes the redundant `status` column; all order status information is preserved in the `order_process_status_id` relationship

## Testing

After migration:
1. Verify existing orders can be queried
2. Test creating new orders
3. Test updating order statuses
4. Verify order filtering by status type works correctly

## Support

If you encounter issues:
1. Check that all order records have a valid `order_process_status_id`
2. Verify that `order_process_statuses` table has entries for your business
3. Review application logs for any entity mapping errors
