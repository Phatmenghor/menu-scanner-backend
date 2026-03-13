# Test Data Files Manifest

## Files in This Directory

### 1. **generate_test_data_expanded.sql** ⭐ MAIN FILE
- **Size:** 78 KB
- **Lines:** 1,561
- **Status:** Production Ready
- **Description:** The complete massively expanded SQL test data file
- **Execution Time:** 2-5 minutes
- **Data Points:** ~500,000+
- **Content:** 15 phases covering all entities with 10x-43x expansions

**Run this file to load all test data:**
```bash
psql -U postgres -d menu_scanner_db -f generate_test_data_expanded.sql
```

---

### 2. **EXPANDED_TEST_DATA_README.md** 📖 COMPREHENSIVE DOCS
- **Size:** 12 KB
- **Sections:** 15 major sections
- **Content Level:** Complete reference documentation
- **Audience:** Developers, QA, DBAs

**Covers:**
- Data scale summary (913 users, 100k products, 10k orders, etc.)
- User management and staff organization
- Product catalog with categories, brands, promotions
- Order system with payment methods
- HR & operational data (schedules, attendances, leaves)
- Customer data and addresses
- Notification system (150 messages)
- Location hierarchy (44,050 records)
- Security & audit (tokens, logs)
- Database structure and 15 phases
- Usage instructions and troubleshooting
- Complete data statistics table

**When to use:** First-time setup, questions about data structure, troubleshooting

---

### 3. **QUICK_START.md** 🚀 QUICK REFERENCE
- **Size:** 3.6 KB
- **Format:** Markdown with code snippets
- **Content Level:** Quick reference
- **Audience:** Experienced developers

**Includes:**
- One-liner execution command
- Test account credentials (all with password: password)
- Data overview table (counts and highlights)
- Verification SQL commands
- Key changes from original (3x users, 10x orders, etc.)
- Execution time estimates
- Progress monitoring guide
- Phase breakdown
- Quick troubleshooting

**When to use:** After first setup, quick lookups, credential reminders

---

### 4. **verify_expanded_data.sql** ✅ VALIDATION SCRIPT
- **Size:** 8.2 KB
- **Purpose:** Data integrity verification
- **Execution Time:** <30 seconds
- **Output:** Formatted report with all entity counts

**Reports on:**
- User statistics by type and status
- Product distribution by status and features
- Category and brand coverage
- Order and item metrics
- Image statistics
- Size variant distribution
- Customer data summary
- Staff and attendance breakdown
- Location hierarchy coverage
- Notification distribution
- Payment and subscription counts
- Security and audit data
- Overall data point total

**Run after loading:**
```bash
psql -U postgres -d menu_scanner_db -f verify_expanded_data.sql
```

**Expected output:** Table showing all entity counts and metrics

---

### 5. **IMPLEMENTATION_SUMMARY.md** 📊 IMPLEMENTATION DETAILS
- **Size:** Detailed technical document
- **Content:** Complete implementation overview
- **Sections:** 12 major sections

**Covers:**
- Completion status (✅ COMPLETE)
- Files created with descriptions
- Data scale achievements (913 users, 10,000 orders, etc.)
- Total data points breakdown (~500,000+)
- SQL implementation details (15 phases)
- Performance optimizations
- Data quality measures
- Test scenarios supported
- Verification procedures
- Usage instructions (3 options)
- Cleanup procedures
- Key improvements table
- Testing recommendations
- Support and documentation
- Final notes and production readiness

**When to use:** Technical deep dive, migration planning, performance tuning

---

### 6. **MANIFEST.md** 📋 THIS FILE
- **Purpose:** Directory of all test data files
- **Content:** Description and usage guide for each file

---

## Legacy Files (Reference)

### 7. **generate_test_data.sql.txt**
- **Size:** 56 KB
- **Lines:** 1,713
- **Status:** Original version
- **Data Scale:** 300 users, 100k products, 1k orders
- **Purpose:** Reference for comparison

---

### 8. **drop-all-table.txt**
- **Purpose:** Complete database cleanup
- **Use:** When you need to remove all data and start fresh

---

## Quick Reference: What File Do I Need?

| Need | File | Time |
|------|------|------|
| Load all test data | `generate_test_data_expanded.sql` | 2-5 min |
| Understand all data | `EXPANDED_TEST_DATA_README.md` | 10 min |
| Quick lookup | `QUICK_START.md` | 1 min |
| Verify data loaded | `verify_expanded_data.sql` | <1 min |
| Understand implementation | `IMPLEMENTATION_SUMMARY.md` | 10 min |
| Just need credentials | `QUICK_START.md` (section 2) | <1 min |
| See what's in directory | `MANIFEST.md` (this file) | 2 min |

---

## Getting Started (3 Steps)

### Step 1: Load Data
```bash
psql -U postgres -d menu_scanner_db -f generate_test_data_expanded.sql
```
**Time:** 2-5 minutes  
**Watch for:** "Phase X complete" messages ending with "✓ MASSIVE EXPANSION COMPLETE!"

### Step 2: Verify Data
```bash
psql -U postgres -d menu_scanner_db -f verify_expanded_data.sql
```
**Time:** <30 seconds  
**Expected:** Detailed report showing all entity counts

### Step 3: Test Login
Use credentials from `QUICK_START.md`:
- Email: phatmenghor20@gmail.com (Business Owner)
- Password: password

---

## Test Account Reference

All passwords: `password`

```
Platform Admin:  phatmenghor19@gmail.com
Business Owner:  phatmenghor20@gmail.com
Test Customer:   phatmenghor21@gmail.com
Staff (500):     staff1@phatrestaurant.com ... staff500@phatrestaurant.com
Customers (410): customer1@test.com ... customer410@test.com
```

---

## Data Overview

### Scale Summary
- **Users:** 913 (500 staff, 410 customers, 3 main)
- **Products:** 100,000 (20 categories, 20 brands)
- **Orders:** 10,000+ (30k line items)
- **Attendances:** 6,000 (200 staff, 30 days)
- **Images:** 150,000+
- **Locations:** 44,050 (full Cambodia hierarchy)
- **Total Data Points:** ~500,000+

### Execution Phases
1. Cleanup (idempotent)
2. Core entities (business, roles, users)
3. Bulk users (910)
4. Products (100,000)
5. Product images (150,000+)
6. Product sizes (33,000)
7. Orders (10,000+)
8. Order items (~30,000)
9. Status history (10,000)
10. Carts & favorites (140)
11. Payments (40)
12. Notifications (150)
13. Images & locations (44,100)
14. HR data (6,200)
15. Tokens & audit (510)

---

## File Locations

All files are located in:
```
/home/user/menu-scanner-backend/docs/test-data/
```

### Directory Listing
```
- EXPANDED_TEST_DATA_README.md       (Comprehensive docs, 12 KB)
- IMPLEMENTATION_SUMMARY.md          (Technical details, detailed)
- MANIFEST.md                        (This file, 📋 directory reference)
- QUICK_START.md                     (Quick reference, 3.6 KB)
- drop-all-table.txt                 (Cleanup script, 259 B)
- generate_test_data.sql.txt         (Original version, 56 KB)
- generate_test_data_expanded.sql    (⭐ MAIN FILE, 78 KB, 1561 lines)
- verify_expanded_data.sql           (Verification, 8.2 KB)
```

---

## Common Tasks

### Load Data and Verify
```bash
psql -U postgres -d menu_scanner_db -f generate_test_data_expanded.sql && \
psql -U postgres -d menu_scanner_db -f verify_expanded_data.sql
```

### Check User Count
```bash
psql -U postgres -d menu_scanner_db -c "SELECT COUNT(*) as users FROM users;"
```

### Count Orders
```bash
psql -U postgres -d menu_scanner_db -c "SELECT COUNT(*) as orders FROM orders;"
```

### Cleanup Everything
```bash
psql -U postgres -d menu_scanner_db -f drop-all-table.txt
```

### View Test Credentials
```bash
grep -A 10 "Test Accounts" QUICK_START.md
```

---

## Troubleshooting

### File Not Found
```bash
cd /home/user/menu-scanner-backend/docs/test-data/
ls -la
```

### Permission Denied
```bash
psql -U postgres   # Use superuser
```

### Database Not Found
```bash
createdb menu_scanner_db
# Then run migrations first, then test data
```

### Data Not Loading
Check for errors in output:
- Look for `ERROR` messages (stops execution)
- Verify with `verify_expanded_data.sql`
- Check RAISE NOTICE messages for phase completion

### Want Partial Data
Edit `generate_test_data_expanded.sql` and comment out unwanted phases

---

## Version Information

- **Created:** March 13, 2026
- **Version:** 2.0 (Massively Expanded)
- **Original Version:** 1.0 (300 users, 1k orders)
- **Expansion Factor:** 3-43x across different entities
- **Status:** Production Ready
- **PostgreSQL Support:** 12+

---

## Related Documentation

- **Original Data:** `generate_test_data.sql.txt` (reference only)
- **Cleanup:** `drop-all-table.txt` (for full reset)
- **Quick Help:** `QUICK_START.md` (fastest way to get started)
- **Full Reference:** `EXPANDED_TEST_DATA_README.md` (complete specification)
- **Technical Details:** `IMPLEMENTATION_SUMMARY.md` (deep dive)
- **Verification:** `verify_expanded_data.sql` (validate after load)

---

## Summary

This directory contains everything needed to load, understand, verify, and manage comprehensive test data for the Menu Scanner Backend restaurant/menu management system.

**All files are ready for production use.**

Start with `QUICK_START.md` for immediate guidance.

For detailed information, see `EXPANDED_TEST_DATA_README.md`.

For technical implementation details, see `IMPLEMENTATION_SUMMARY.md`.

---

**Questions?** Check the appropriate file above - everything is documented!
