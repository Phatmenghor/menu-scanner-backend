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
- Random payment statuses (70% COMPLETED, 20% PENDING, 5% FAILED, 5% CANCELLED)
- Random order statuses distributed across all 8 statuses
- Created dates spread over last 90 days
- Realistic delivery address snapshots

### 3. `03_insert_order_related_data.sql`
Populates all related data for the 7,000 orders:

**Order Items**:
- 1-5 items per order (automatically uses products from the business)
- Random quantities (1-3 per item)
- 30% have promotions (5-25% off or $1-4 fixed discount)
- 20% have special instructions (extra sauce, no veggies, less spicy)
- Realistic pricing with snapshots

**Order Status History**:
- Tracks complete status progression for each order
- Realistic timestamps between status changes
- Changed by system/business/customer/driver
- Notes for each status change

**Business Order Payments**:
- One payment record per order (except cancelled)
- Unique payment references (PAY-YYYYMMDD-XXXXXX)
- Matches order payment method and status
- Proper timestamps based on payment status

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

### Step 3: Run Order Related Data Script
1. In the Query Tool, open `03_insert_order_related_data.sql`
2. Click **Execute** (F5)
3. Wait for completion (may take 1-2 minutes)
4. You'll see progress messages every 500 orders
5. Final statistics will show:
   - Total order items created (with promotions and special instructions)
   - Order status history records
   - Business order payment records
   - Average items per order

**Requirements**:
- Script 01 and 02 must be run first
- Your business must have products in the database
- Products must be available and not deleted

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

-- Order items statistics
SELECT
    COUNT(*) as total_items,
    AVG(quantity) as avg_quantity,
    COUNT(CASE WHEN has_promotion THEN 1 END) as items_with_promotion,
    COUNT(CASE WHEN special_instructions IS NOT NULL THEN 1 END) as items_with_instructions
FROM order_items oi
JOIN orders o ON oi.order_id = o.id
WHERE o.business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828';

-- Order status history statistics
SELECT
    ops.name as status_name,
    COUNT(*) as transition_count
FROM order_status_history osh
JOIN order_process_statuses ops ON osh.order_process_status_id = ops.id
JOIN orders o ON osh.order_id = o.id
WHERE o.business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828'
GROUP BY ops.name
ORDER BY transition_count DESC;

-- Payment statistics
SELECT
    status,
    COUNT(*) as payment_count,
    SUM(amount) as total_amount
FROM business_order_payments
WHERE business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828'
GROUP BY status
ORDER BY payment_count DESC;

-- Complete order with items
SELECT
    o.order_number,
    o.order_process_status_name,
    o.total_amount,
    COUNT(oi.id) as item_count,
    STRING_AGG(oi.product_name || ' x' || oi.quantity, ', ') as items
FROM orders o
LEFT JOIN order_items oi ON o.id = oi.order_id
WHERE o.business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828'
GROUP BY o.id, o.order_number, o.order_process_status_name, o.total_amount
ORDER BY o.created_at DESC
LIMIT 10;
```

## 🧹 Clean Up (If Needed)

To remove all test orders and related data:

```sql
-- Delete in reverse order of creation (foreign key constraints)

-- 1. Delete business order payments
DELETE FROM business_order_payments
WHERE order_id IN (
    SELECT id FROM orders
    WHERE business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828'
      AND customer_id = 'b00bde3f-5287-4eec-bfe5-1daa2b2a44ba'
);

-- 2. Delete order status history
DELETE FROM order_status_history
WHERE order_id IN (
    SELECT id FROM orders
    WHERE business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828'
      AND customer_id = 'b00bde3f-5287-4eec-bfe5-1daa2b2a44ba'
);

-- 3. Delete order items
DELETE FROM order_items
WHERE order_id IN (
    SELECT id FROM orders
    WHERE business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828'
      AND customer_id = 'b00bde3f-5287-4eec-bfe5-1daa2b2a44ba'
);

-- 4. Delete orders
DELETE FROM orders
WHERE business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828'
  AND customer_id = 'b00bde3f-5287-4eec-bfe5-1daa2b2a44ba';

-- 5. Delete order process statuses (if needed)
DELETE FROM order_process_statuses
WHERE business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828';
```

## 📝 Notes

- **Order Numbers**: Generated in format `ORD-YYYYMMDD-XXXXXX` (e.g., `ORD-20260211-000001`)
- **Payment Status Distribution**:
  - 70% COMPLETED
  - 20% PENDING
  - 5% FAILED
  - 5% CANCELLED
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
