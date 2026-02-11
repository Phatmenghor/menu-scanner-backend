# SQL Scripts for Order Data Generation

This directory contains SQL scripts to populate your database with order process statuses and test orders.

## 📋 Scripts Overview

### 1. `01_insert_order_process_statuses.sql`
Creates 8 order process statuses for your business:
- **Pending** - Order placed, waiting for confirmation
- **Confirmed** - Order confirmed by restaurant
- **Preparing** - Order being prepared in kitchen
- **Ready** - Order ready for pickup/delivery
- **Out for Delivery** - Order is out for delivery
- **Delivered** - Order delivered to customer
- **Completed** - Order completed successfully
- **Cancelled** - Order cancelled

### 2. `02_insert_7000_orders.sql`
Generates **7,000 test orders** with:
- Business ID: `0a32d15e-1da6-4c39-bbe7-eec305035828`
- Customer ID: `b00bde3f-5287-4eec-bfe5-1daa2b2a44ba`
- Random order numbers (e.g., `ORD-20260211-000001`)
- Random pricing (subtotal: $5-$100)
- Random delivery fees ($0, $2, $3, or $5)
- Random payment methods (CASH, BANK_TRANSFER, ONLINE, OTHER)
- Random payment statuses (80% PAID, 15% UNPAID, 5% PARTIALLY_PAID)
- Random order statuses distributed across all 8 statuses
- Created dates spread over last 90 days
- Realistic delivery address snapshots

### 3. `03_insert_order_items_sample.sql` (Optional)
Creates order items for the first 100 orders:
- 1-5 items per order
- Random product names
- Random quantities (1-3)
- Random pricing
- 20% chance of promotions
- Optional special instructions

⚠️ **Note**: This requires actual product IDs from your database. You'll need to update the script with real product IDs.

## 🚀 How to Use in pgAdmin

### Step 1: Run Order Process Statuses Script
1. Open **pgAdmin**
2. Connect to your database
3. Right-click on your database → **Query Tool**
4. Open `01_insert_order_process_statuses.sql`
5. Click **Execute** (F5)
6. Verify: You should see 8 statuses created

### Step 2: Run Orders Script
1. In the Query Tool, open `02_insert_7000_orders.sql`
2. Click **Execute** (F5)
3. Wait for completion (may take 30-60 seconds)
4. You'll see progress messages every 1000 orders
5. Final verification queries will show:
   - Order count by status and payment status
   - Total order count (should be 7000)
   - Sample of 20 most recent orders

### Step 3: (Optional) Run Order Items Script
1. First, get actual product IDs from your database:
   ```sql
   SELECT id, name FROM products
   WHERE business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828'
   LIMIT 10;
   ```
2. Open `03_insert_order_items_sample.sql`
3. Replace the placeholder UUIDs in `v_product_ids` array with real product IDs
4. Update `v_product_names` array with actual product names
5. Click **Execute** (F5)

## 📊 Verification Queries

After running the scripts, you can verify the data:

```sql
-- Check total orders
SELECT COUNT(*)
FROM orders
WHERE business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828';

-- Orders by status
SELECT
    order_process_status_name,
    COUNT(*) as count,
    ROUND(AVG(total_amount), 2) as avg_amount
FROM orders
WHERE business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828'
GROUP BY order_process_status_name
ORDER BY count DESC;

-- Orders by payment status
SELECT
    payment_status,
    payment_method,
    COUNT(*) as count
FROM orders
WHERE business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828'
GROUP BY payment_status, payment_method
ORDER BY payment_status, count DESC;

-- Orders created per day (last 30 days)
SELECT
    DATE(created_at) as order_date,
    COUNT(*) as orders_count,
    SUM(total_amount) as total_revenue
FROM orders
WHERE business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828'
  AND created_at >= NOW() - INTERVAL '30 days'
GROUP BY DATE(created_at)
ORDER BY order_date DESC;
```

## 🧹 Clean Up (If Needed)

To remove all test orders:

```sql
-- Delete order items first (if created)
DELETE FROM order_items
WHERE order_id IN (
    SELECT id FROM orders
    WHERE business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828'
      AND customer_id = 'b00bde3f-5287-4eec-bfe5-1daa2b2a44ba'
);

-- Delete orders
DELETE FROM orders
WHERE business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828'
  AND customer_id = 'b00bde3f-5287-4eec-bfe5-1daa2b2a44ba';

-- Delete order process statuses (if needed)
DELETE FROM order_process_statuses
WHERE business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828';
```

## 📝 Notes

- **Order Numbers**: Generated in format `ORD-YYYYMMDD-XXXXXX` (e.g., `ORD-20260211-000001`)
- **Payment Status Distribution**:
  - 80% PAID
  - 15% UNPAID
  - 5% PARTIALLY_PAID
- **Date Range**: Orders spread randomly over last 90 days
- **Delivery Fees**:
  - $0 = Pickup
  - $2 = Within 2km
  - $3 = Within 5km
  - $5 = Within 10km
- **Performance**: Script includes progress notifications every 1000 orders
- **Idempotency**: Order process status script uses `ON CONFLICT DO NOTHING` to prevent duplicates

## ⚠️ Important

- Always test on a **development/staging database** first
- Back up your database before running bulk inserts
- The order items script requires actual product IDs from your database
- Execution time may vary based on database performance (typically 30-60 seconds for 7000 orders)

## 🔧 Customization

You can modify the scripts to:
- Change the number of orders (modify loop range in script 2)
- Adjust payment status distribution (modify the CASE statement)
- Change date range (modify `RANDOM() * INTERVAL '90 days'`)
- Customize pricing ranges (modify subtotal and delivery fee ranges)
- Add different order statuses or payment methods

## 📞 Support

If you encounter any issues:
1. Check that the business_id and customer_id exist in your database
2. Verify table schemas match the entity models
3. Check database permissions for INSERT operations
4. Review PostgreSQL logs for detailed error messages
