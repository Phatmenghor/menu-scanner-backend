# Quick Start: Expanded Test Data

## One-Liner Execution

```bash
psql -U postgres -d menu_scanner_db -f generate_test_data_expanded.sql
```

## Test Credentials

```
All accounts use password: password

Platform:    phatmenghor19@gmail.com (PLATFORM_ADMIN)
Business:    phatmenghor20@gmail.com (BUSINESS_ADMIN) 
Customer:    phatmenghor21@gmail.com (CUSTOMER)
Staff:       staff1@phatrestaurant.com ... staff500@phatrestaurant.com
Customers:   customer1@test.com ... customer410@test.com
```

## Data at a Glance

| What | Count | Highlights |
|------|-------|-----------|
| **Users** | 913 | 500 staff, 410 customers, 3 main |
| **Products** | 100,000+ | 20 categories, 14% promoted |
| **Orders** | 10,000+ | ~30k line items, 71% completed |
| **Attendances** | 6,000 | 200 staff, 30 days, realistic statuses |
| **Location Data** | 44,050 | Full province → village hierarchy |
| **Notifications** | 150 | 50 per role type |

## Verification Commands

```sql
-- Quick data checks
SELECT COUNT(*) FROM users;                    -- Expected: 913
SELECT COUNT(*) FROM products;                 -- Expected: 100,000
SELECT COUNT(*) FROM orders;                   -- Expected: 10,000+
SELECT COUNT(*) FROM attendances;              -- Expected: 6,000
SELECT COUNT(*) FROM order_items;              -- Expected: ~30,000
SELECT COUNT(*) FROM location_village_cbc;     -- Expected: 40,000
```

## Key Changes from Original

- **Users:** 300 → 913 (3x expansion, 500 staff vs 100)
- **Orders:** 1,000 → 10,000 (10x expansion)
- **Products:** 100,000 (kept same, already substantial)
- **Attendances:** 140 → 6,000 (43x expansion for HR testing)
- **Images:** ~120,000 → 150,000+
- **HR Data:** 20 staff schedules → 200, comprehensive leave tracking
- **Notifications:** 50 → 150, better testing coverage
- **Audit Logs:** 0 → 200, security testing

## File Location

```
/home/user/menu-scanner-backend/docs/test-data/generate_test_data_expanded.sql
```

## Execution Time

- **Typical:** 2-3 minutes
- **Slow system:** 3-5 minutes
- **Fast system:** 1-2 minutes

## Monitor Progress

Watch console output for phase completion messages:
```
Phase 1 complete — core entities created.
Phase 2 complete — 910 extra users created.
Phase 3 complete — 100,000 products created.
[... continues through Phase 15 ...]
✓ MASSIVE EXPANSION COMPLETE!
```

## Troubleshooting

**Error: "pgcrypto not found"**
```sql
CREATE EXTENSION IF NOT EXISTS pgcrypto;
```

**Error: "permission denied"**
```bash
psql -U postgres  # Use superuser account
```

**Error: "relation does not exist"**
- Run database migrations first
- Ensure schema is initialized

**Data not appearing?**
- Check execution completed (look for final RAISE NOTICE)
- Verify with quick data checks above
- Check database didn't rollback (look for ERROR messages)

## Next Steps

1. ✓ Execute SQL file
2. ✓ Verify counts match expected
3. Start development/testing
4. Use test credentials to log in
5. Query as needed for specific test scenarios

## Advanced: Partial Data

To load only specific phases, edit the SQL file and comment out unwanted phases.

**Phases:**
- Phase 0: Cleanup
- Phase 1: Core entities
- Phase 2: Bulk users
- Phase 3: Products
- Phase 4: Product images
- Phase 5: Product sizes
- Phase 6: Orders
- Phase 7: Order items
- Phase 8: Status history
- Phase 9: Carts/favorites
- Phase 10: Payments
- Phase 11: Notifications
- Phase 12: Images & locations
- Phase 13: HR data
- Phase 14-15: Tokens, sessions, audit logs

---

**Need help?** Check EXPANDED_TEST_DATA_README.md for detailed documentation.
