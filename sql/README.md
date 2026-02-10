# Order Data Generation SQL Scripts

SQL scripts to generate test data directly in PostgreSQL (pgAdmin).

## Business ID
```
0a32d15e-1da6-4c39-bbe7-eec305035828
```

## Scripts

### 1. `generate_order_statuses.sql`
Creates 8 order process statuses for the business:
- Pending
- Confirmed
- Preparing
- Ready
- Out for Delivery
- Delivered
- Completed
- Cancelled

**Execution time**: < 1 second

### 2. `generate_80000_orders.sql`
Generates 80,000 orders with realistic data:
- **Date Range**: Feb 10, 2025 to Feb 10, 2027 (2 years)
- **Items per Order**: 1-5 random items
- **Products**: 10 Cambodian food items
- **Pricing**: $3-$15 per item
- **Discounts**: 30% of items have 20% off
- **Delivery**: 70% of orders have delivery ($1-$5 fee)
- **Payment Methods**: Random (CASH, CREDIT_CARD, DEBIT_CARD, MOBILE_PAYMENT)
- **Status Distribution**: Random across all statuses

**Execution time**: ~5-10 minutes (depending on server)

## How to Use in pgAdmin

### Step 1: Generate Order Statuses
1. Open pgAdmin
2. Connect to your database
3. Open Query Tool (Tools → Query Tool)
4. Open file: `generate_order_statuses.sql`
5. Click Execute (F5)
6. Verify: You should see 8 statuses created

### Step 2: Generate 80,000 Orders
1. In the same Query Tool
2. Open file: `generate_80000_orders.sql`
3. Click Execute (F5)
4. Wait for completion (you'll see progress notifications every 10,000 orders)
5. Final results will show:
   - Total orders created
   - Date range
   - Average order value
   - Total revenue
   - Orders per status

## Expected Results

After running both scripts:

```sql
-- Order Statuses
total_statuses: 8

-- Orders
total_orders: 80,000
earliest_order: 2025-02-10
latest_order: 2027-02-10
avg_order_value: ~$20-30
total_revenue: ~$1.6M - $2.4M

-- Items
total_items: ~200,000 (avg 2.5 items per order)
```

## Verification Queries

### Check order distribution by status
```sql
SELECT
    ops.name,
    COUNT(*) as order_count,
    ROUND(AVG(o.total_amount), 2) as avg_amount
FROM orders o
JOIN order_process_statuses ops ON o.order_process_status_id = ops.id
WHERE o.business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828'
GROUP BY ops.name
ORDER BY order_count DESC;
```

### Check orders by month
```sql
SELECT
    TO_CHAR(created_at, 'YYYY-MM') as month,
    COUNT(*) as orders,
    ROUND(SUM(total_amount), 2) as revenue
FROM orders
WHERE business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828'
GROUP BY TO_CHAR(created_at, 'YYYY-MM')
ORDER BY month;
```

### Check top products
```sql
SELECT
    product_name,
    COUNT(*) as times_ordered,
    ROUND(AVG(unit_price), 2) as avg_price
FROM order_items oi
JOIN orders o ON oi.order_id = o.id
WHERE o.business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828'
GROUP BY product_name
ORDER BY times_ordered DESC;
```

## Notes

- The script uses `gen_random_uuid()` for generating UUIDs
- Progress is logged every 10,000 orders
- All dates are random within the 2-year range
- Order numbers follow format: `ORD-YYYYMMDD-XXXXXX`
- Script is idempotent for statuses (ON CONFLICT DO NOTHING)

## Cleanup (if needed)

To remove generated data:

```sql
-- Delete orders and items for this business
DELETE FROM order_items
WHERE order_id IN (
    SELECT id FROM orders
    WHERE business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828'
);

DELETE FROM orders
WHERE business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828';

-- Delete statuses
DELETE FROM order_process_statuses
WHERE business_id = '0a32d15e-1da6-4c39-bbe7-eec305035828';
```
