# Massively Expanded Test Data for Menu Scanner Backend

## Overview

This document describes the **massively expanded SQL test data file** designed for comprehensive development and testing of the Menu Scanner restaurant/menu management system.

**File:** `generate_test_data_expanded.sql`  
**Size:** ~78KB  
**Lines:** 1,561  
**Estimated Execution Time:** 2-5 minutes (depending on database performance)

---

## Data Scale Summary

### Users: **913 Total**
- **500 Staff** (business users) — various positions: Chef, Sous Chef, Waiter, Cashier, Manager, Barista, Kitchen Helper, Host
- **410 Customers** (customer users)
- **3 Main Test Accounts** (Platform Admin, Business Owner, Customer)
- All accounts with pre-generated addresses, full contact details, and activity history

### Products: **100,000+**
- All with realistic pricing ($1.50–$25.00)
- **Categories:** 20 (Cambodian, Thai, Vietnamese, Chinese, Western, Breakfast, Lunch, Dinner, Beverages, etc.)
- **Brands:** 20 (House Special, Chef's Selection, Premium Quality, Organic, Signature, etc.)
- **Promotion Coverage:** ~14% of products have active promotional discounts (5–30% off)
- **Status Distribution:** 80% ACTIVE | 10% INACTIVE | 10% OUT_OF_STOCK
- **Sizes:** ~33,000 product size records (Small, Medium, Large, Extra Large with price adjustments)

### Orders: **10,000+ Orders**
- **10x expansion from original 1,000 orders**
- ~30,000 order line items (2–4 items per order on average)
- Various payment methods: CASH, CARD, ONLINE, MOBILE_WALLET, BANK_TRANSFER
- ~71% order completion rate
- Full order status history tracking
- Diverse customer base (main customer + 410 random customers)

### Product Images: **150,000+**
- 1 image per product (100,000 records)
- Additional images for ~50% of products (50,000 records)
- Structured for batch image retrieval operations

### Customer Data
- **Addresses:** 410 (one per customer)
- **Cart Items:** 40 (main customer's active cart)
- **Favorites:** 100 (main customer's favorite products)
- **Realistic Locations:** Phnom Penh districts and commune-level coverage

---

## HR & Operational Data

### Work Schedules & Attendance
- **Schedules:** 200 staff with diverse shift patterns
  - Morning Shift (6:00–14:00)
  - Afternoon Shift (14:00–22:00)
  - Full Day (8:00–17:00)
  - Split Shift (flexible)
  - Evening Shift (18:00–23:00)
- **Attendances:** 6,000 records (200 staff × 30 days of history)
- **Status Distribution:** ~80% PRESENT | 10% LATE | 10% ABSENT
- **Check-Ins:** ~11,000 (START + END records per attendance)
  - GPS coordinates (Phnom Penh area)
  - Timestamp variation for realism

### Leave Management
- **Leave Types:** 8 categories (Annual, Sick, Personal, Bereavement, Maternity, Paternity, Unpaid, Training)
- **Leave Records:** 100 spanning various staff
- **Status Variety:** PENDING, APPROVED, REJECTED
- **Approval Chain:** Business owner approvals tracked

---

## Payments & Subscriptions

### Payment History
- **40 payment records** (vs. 18 in original)
- **Subscription Plans:** 20 different tiers
  - Annual Premium: $299.99/year (MAIN PLAN USED)
  - Premium Monthly: $29.99/month
  - Basic Monthly: $9.99/month
  - Free Trial: 0.00 (14 days)
  - Plus 16 additional tiers and variants
- **Status Distribution:** 70% COMPLETED | 15% PENDING | 10% REFUNDED | 5% FAILED
- **Payment Methods:** ONLINE, BANK_TRANSFER, CASH

### Order Payments
- **Business Order Payments:** 50 records
- Payment tracking for order fulfillment
- Linked to actual orders and customers

---

## Notifications & Communications

### Expanded Notification System
- **Total:** 150+ notifications
- **Platform Admin:** 50 notifications (new business registrations, system alerts, reports, maintenance notices)
- **Business Owner:** 50 notifications (new orders, daily reports, subscription reminders, staff alerts, low stock warnings)
- **Customer:** 50 notifications (order confirmations, delivery updates, promotions, review requests, loyalty points)
- **Status Tracking:** Seen/read status with timestamps
- **Priority Levels:** HIGH, NORMAL, LOW

---

## Location Hierarchy Data

### Geographic Coverage
- **Provinces:** 40 (all Cambodian provinces)
- **Districts:** 400 (10 per province)
- **Communes:** 4,000 (10 per district)
- **Villages:** 40,000 (10 per commune)
- **Total Location Records:** 44,050

**Purpose:** Support full location-based filtering, delivery area management, and customer address validation.

---

## Security & Audit

### Token Management
- **Refresh Tokens:** 50 active tokens
- **User Sessions:** 80 sessions (various users)
- **Blacklisted Tokens:** 30 revoked tokens
- **Session Metadata:** IP addresses, user agents, activity timestamps

### Audit Logs
- **200 comprehensive audit log entries** tracking:
  - CREATE, UPDATE, DELETE operations
  - LOGIN, LOGOUT events
  - VIEW, EXPORT actions
  - Resource types: Orders, Products, Users, Payments, Businesses, Categories, Carts, Subscriptions
  - IP addresses and user agents
  - Success/failure status

---

## Delivery & Operations

### Configuration
- **20 Delivery Options:**
  - Dine In, Takeaway, Standard Delivery, Express Delivery
  - Same Day Delivery, Scheduled Delivery, Curbside Pickup
  - Drive-Thru, Room Service, Office Delivery
  - Event Catering, Pre-Order Pickup, Night Delivery
  - Breakfast Delivery, Eco Delivery, Premium Delivery
  - Contactless Delivery, Group Order, Party Pack, International Ship

- **20 Order Process Statuses:**
  - Full lifecycle tracking: Pending → Confirmed → Preparing → Ready → Delivered
  - Additional states: On Hold, Scheduled, At Counter, Quality Check
  - Resolution states: Cancelled, Rejected, Refunded, Return Requested, Completed

### Business Settings
- Single business: "Phat Restaurant & Café"
- Operating hours: 8:00–22:00 (7 days/week)
- Service options: Dine-in, Takeaway, Multiple delivery types
- Currency: USD with KHR conversion (4,100 KHR/USD)
- Tax rate: 10% | Service charge: 5%

---

## Test Accounts

### Main Accounts (password: `password`)

```
Platform Admin:
  Email: phatmenghor19@gmail.com
  Role: PLATFORM_ADMIN
  Access: Full platform administration

Business Owner:
  Email: phatmenghor20@gmail.com
  Role: BUSINESS_ADMIN
  Business: Phat Restaurant & Café
  Access: Complete business management

Test Customer:
  Email: phatmenghor21@gmail.com
  Role: CUSTOMER
  Access: Customer ordering and reviews
```

### Bulk Accounts

```
Staff: staff1@phatrestaurant.com to staff500@phatrestaurant.com
  (500 accounts spanning all staff positions)

Customers: customer1@test.com to customer410@test.com
  (410 customer accounts with addresses)

All use: password
```

---

## Database Structure

### 15 Execution Phases

1. **Phase 0:** Cleanup (idempotent deletion in FK-safe order)
2. **Phase 1:** Core entities (business, roles, users, categories, brands, subscriptions, settings)
3. **Phase 2:** Bulk users (500 staff + 410 customers)
4. **Phase 3:** Products (100,000 records with deterministic pricing/promotions)
5. **Phase 4:** Product images (150,000+ records, 1-2 per product)
6. **Phase 5:** Product sizes (~33,000 size variants)
7. **Phase 6:** Massive orders (10,000 orders)
8. **Phase 7:** Order items (~30,000 line items)
9. **Phase 8:** Order status history (10,000 status change records)
10. **Phase 9:** Cart and favorites (40 cart items, 100 favorites)
11. **Phase 10:** Payment history (40 subscription payments)
12. **Phase 11:** Notifications (150 total)
13. **Phase 12:** Images and location hierarchy (44,050 location records)
14. **Phase 13:** HR data (work schedules, 6,000+ attendances, 100 leaves)
15. **Phase 14–15:** Payments, tokens, sessions, audit logs

---

## Key Features

### Performance Optimization
- ✓ Uses `generate_series()` for bulk inserts (much faster than row-by-row)
- ✓ Deterministic data generation (HASHTEXT) instead of RANDOM() for reproducibility
- ✓ Idempotent cleanup phase (safe to re-run)
- ✓ Batch inserts grouped by logical entity

### Data Quality
- ✓ Realistic product pricing and promotion coverage
- ✓ Proper foreign key relationships maintained
- ✓ Activity timestamps with realistic spread
- ✓ Status distributions matching real-world patterns
- ✓ Geographic location hierarchy for Cambodia

### Development Support
- ✓ Comprehensive test data for all features
- ✓ Edge cases: cancelled orders, failed payments, absent staff
- ✓ Multiple customer types and roles
- ✓ Full audit trail for compliance testing
- ✓ Enough variety for pagination and filtering tests

---

## Usage Instructions

### Prerequisites
- PostgreSQL 12+ with pgcrypto extension
- Menu Scanner Backend database initialized
- 2–5 minutes for full execution (depending on hardware)

### Execution

```bash
# Option 1: Using psql CLI
psql -U username -d database_name -f generate_test_data_expanded.sql

# Option 2: Using pgAdmin
# 1. Open pgAdmin Query Tool
# 2. Copy entire file contents
# 3. Execute (F5)
# 4. Monitor RAISE NOTICE messages for progress
```

### Verification

After execution, verify data:

```sql
-- Check user count
SELECT COUNT(*) AS total_users FROM users;
-- Expected: 913

-- Check products
SELECT COUNT(*) AS total_products FROM products;
-- Expected: 100,000

-- Check orders
SELECT COUNT(*) AS total_orders FROM orders;
-- Expected: 10,000

-- Check order items
SELECT COUNT(*) AS total_items FROM order_items;
-- Expected: ~30,000

-- Check location data
SELECT COUNT(*) AS total_locations FROM location_village_cbc;
-- Expected: 40,000
```

---

## Cleanup

To remove all expanded test data:

```bash
psql -U username -d database_name -f drop-all-table.txt
```

Or delete selectively:

```sql
DELETE FROM users WHERE email LIKE 'staff%@phatrestaurant.com' 
                        OR email LIKE 'customer%@test.com';
```

---

## Data Summary Statistics

| Entity | Count | Notes |
|--------|-------|-------|
| Users | 913 | 500 staff + 410 customers + 3 main |
| Products | 100,000 | 80% active, 20 categories, 20 brands |
| Product Images | 150,000+ | 1-2 per product |
| Product Sizes | ~33,000 | 4 sizes per applicable product |
| Orders | 10,000+ | 10x expansion from original |
| Order Items | ~30,000 | 2-4 per order average |
| Notifications | 150 | 50 each: platform, business, customer |
| Attendances | 6,000 | 200 staff × 30 days |
| Check-Ins | ~11,000 | START + END per attendance |
| Leaves | 100 | Various types and staff |
| Payments | 40+ | Subscription + order payments |
| Locations | 44,050 | Provinces → districts → communes → villages |
| **TOTAL** | **~500,000+** | Comprehensive test coverage |

---

## Notes

- All timestamps are relative to `NOW()` for portability
- Password for all accounts: `bcrypt("password")`
- Subscription auto-renewal enabled
- Business currency: USD
- Tax & service charge calculations included in order totals
- GPS coordinates centered on Phnom Penh, Cambodia (±0.01 degree variation)

---

## Support

For questions or issues with test data:
1. Verify PostgreSQL version (12+)
2. Check pgcrypto extension is loaded
3. Review RAISE NOTICE messages for phase completion
4. Validate foreign key constraints are enabled
5. Ensure adequate disk space (~500MB+ for 500k records)

---

**Generated:** March 2026  
**Version:** 2.0 (Massively Expanded)  
**Original Scale:** 300 users, 100k products, 1k orders  
**New Scale:** 913 users, 100k+ products, 10k+ orders, 500k+ data points
