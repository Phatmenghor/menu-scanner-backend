# Database Migration Instructions

## Issue: Application Fails to Start Due to `leave_type_enum_id` Column

If you're seeing this error when starting the application:

```
ERROR: column "leave_type_enum_id" of relation "leaves" contains null values
```

This is because Hibernate is trying to add a new column but there are existing records in the `leaves` table.

## Solution: Run Manual Migration

### Option 1: Run the migration script directly

```bash
psql -h 165.22.247.142 -U postgres -d e_menu_platform -f migration_add_leave_type_enum_id.sql
```

When prompted, enter the password: `Hour1819`

### Option 2: Run the SQL command manually

Connect to the database:

```bash
psql -h 165.22.247.142 -U postgres -d e_menu_platform
```

Then run:

```sql
ALTER TABLE leaves ADD COLUMN IF NOT EXISTS leave_type_enum_id UUID NULL;
```

### Option 3: Use a database client

1. Open your preferred database client (DBeaver, pgAdmin, etc.)
2. Connect to the database:
   - Host: `165.22.247.142`
   - Port: `5432`
   - Database: `e_menu_platform`
   - User: `postgres`
   - Password: `Hour1819`
3. Execute the SQL:
   ```sql
   ALTER TABLE leaves ADD COLUMN IF NOT EXISTS leave_type_enum_id UUID NULL;
   ```

## After Migration

Once the column is added, the application should start successfully. The `leave_type_enum_id` field is nullable, so existing records can have NULL values. New leave requests will require a leave type to be specified.

## Future: Add Index (Optional)

Once all leave records have valid `leave_type_enum_id` values, you can add an index for better performance:

```sql
CREATE INDEX IF NOT EXISTS idx_leave_type ON leaves(leave_type_enum_id);
```

You can also make the column NOT NULL if desired:

```sql
-- First, ensure all records have values
UPDATE leaves SET leave_type_enum_id = '<default-leave-type-uuid>' WHERE leave_type_enum_id IS NULL;

-- Then make it NOT NULL
ALTER TABLE leaves ALTER COLUMN leave_type_enum_id SET NOT NULL;
```
