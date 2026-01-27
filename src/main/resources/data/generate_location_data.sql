-- =====================================================================
-- E-Menu Platform: Cambodia Location Data Generation (OPTIMIZED)
-- Generates: 25 Provinces, 2,500 Districts, 250,000 Communes, 25,000,000 Villages
--
-- OPTIMIZED for speed:
--   1) TRUNCATE tables (fresh start — no NOT EXISTS overhead)
--   2) DROP indexes before bulk insert
--   3) Insert without duplicate checks (clean tables)
--   4) RECREATE indexes after insert
--   5) ANALYZE for query planner
--
-- Code format (each level appends 2 digits from parent):
--   Province: PP           → 01, 02, ... 25
--   District: PP+DD        → 0100, 0101, ... 0199
--   Commune:  PPDD+CC      → 010000, 010001, ... 010099
--   Village:  PPDDCC+VV    → 01000000, 01000001, ... 01000099
-- =====================================================================

-- =====================================================================
-- STEP 0: CLEAN START — truncate all location tables
-- =====================================================================
TRUNCATE location_village_cbc  CASCADE;
TRUNCATE location_commune_cbc  CASCADE;
TRUNCATE location_district_cbc CASCADE;
TRUNCATE location_province_cbc CASCADE;

-- =====================================================================
-- STEP 1: DROP ALL INDEXES on village table (biggest table)
--         This prevents index maintenance during 25M inserts.
-- =====================================================================
DROP INDEX IF EXISTS idx_village_code;
DROP INDEX IF EXISTS idx_village_commune;
DROP INDEX IF EXISTS idx_village_deleted;
ALTER TABLE location_village_cbc DROP CONSTRAINT IF EXISTS uk_village_code;

-- Also drop commune indexes (250K rows)
DROP INDEX IF EXISTS idx_commune_code;
DROP INDEX IF EXISTS idx_commune_district;
DROP INDEX IF EXISTS idx_commune_deleted;
ALTER TABLE location_commune_cbc DROP CONSTRAINT IF EXISTS uk_commune_code;

-- Also drop district indexes (2,500 rows)
DROP INDEX IF EXISTS idx_district_code;
DROP INDEX IF EXISTS idx_district_province;
DROP INDEX IF EXISTS idx_district_deleted;
ALTER TABLE location_district_cbc DROP CONSTRAINT IF EXISTS uk_district_code;

-- =====================================================================
-- STEP 2: INSERT 25 PROVINCES (instant)
-- =====================================================================
INSERT INTO location_province_cbc (id, province_code, province_en, province_kh, version, is_deleted, created_at, updated_at, created_by, updated_by)
VALUES
    (gen_random_uuid(), '01', 'Banteay Meanchey',  'បន្ទាយមានជ័យ',   0, false, NOW(), NOW(), 'system', 'system'),
    (gen_random_uuid(), '02', 'Battambang',         'បាត់ដំបង',       0, false, NOW(), NOW(), 'system', 'system'),
    (gen_random_uuid(), '03', 'Kampong Cham',       'កំពង់ចាម',       0, false, NOW(), NOW(), 'system', 'system'),
    (gen_random_uuid(), '04', 'Kampong Chhnang',    'កំពង់ឆ្នាំង',     0, false, NOW(), NOW(), 'system', 'system'),
    (gen_random_uuid(), '05', 'Kampong Speu',       'កំពង់ស្ពឺ',       0, false, NOW(), NOW(), 'system', 'system'),
    (gen_random_uuid(), '06', 'Kampong Thom',       'កំពង់ធំ',        0, false, NOW(), NOW(), 'system', 'system'),
    (gen_random_uuid(), '07', 'Kampot',             'កំពត',           0, false, NOW(), NOW(), 'system', 'system'),
    (gen_random_uuid(), '08', 'Kandal',             'កណ្ដាល',         0, false, NOW(), NOW(), 'system', 'system'),
    (gen_random_uuid(), '09', 'Koh Kong',           'កោះកុង',         0, false, NOW(), NOW(), 'system', 'system'),
    (gen_random_uuid(), '10', 'Kratie',             'ក្រចេះ',          0, false, NOW(), NOW(), 'system', 'system'),
    (gen_random_uuid(), '11', 'Mondulkiri',         'មណ្ឌលគិរី',       0, false, NOW(), NOW(), 'system', 'system'),
    (gen_random_uuid(), '12', 'Phnom Penh',         'ភ្នំពេញ',         0, false, NOW(), NOW(), 'system', 'system'),
    (gen_random_uuid(), '13', 'Preah Vihear',       'ព្រះវិហារ',       0, false, NOW(), NOW(), 'system', 'system'),
    (gen_random_uuid(), '14', 'Prey Veng',          'ព្រៃវែង',         0, false, NOW(), NOW(), 'system', 'system'),
    (gen_random_uuid(), '15', 'Pursat',             'ពោធិ៍សាត់',       0, false, NOW(), NOW(), 'system', 'system'),
    (gen_random_uuid(), '16', 'Ratanakiri',         'រតនគិរី',         0, false, NOW(), NOW(), 'system', 'system'),
    (gen_random_uuid(), '17', 'Siem Reap',          'សៀមរាប',         0, false, NOW(), NOW(), 'system', 'system'),
    (gen_random_uuid(), '18', 'Preah Sihanouk',     'ព្រះសីហនុ',       0, false, NOW(), NOW(), 'system', 'system'),
    (gen_random_uuid(), '19', 'Stung Treng',        'ស្ទឹងត្រែង',      0, false, NOW(), NOW(), 'system', 'system'),
    (gen_random_uuid(), '20', 'Svay Rieng',         'ស្វាយរៀង',        0, false, NOW(), NOW(), 'system', 'system'),
    (gen_random_uuid(), '21', 'Takeo',              'តាកែវ',          0, false, NOW(), NOW(), 'system', 'system'),
    (gen_random_uuid(), '22', 'Oddar Meanchey',     'ឧត្ដរមានជ័យ',     0, false, NOW(), NOW(), 'system', 'system'),
    (gen_random_uuid(), '23', 'Kep',                'កែប',            0, false, NOW(), NOW(), 'system', 'system'),
    (gen_random_uuid(), '24', 'Pailin',             'ប៉ៃលិន',          0, false, NOW(), NOW(), 'system', 'system'),
    (gen_random_uuid(), '25', 'Tboung Khmum',       'ត្បូងឃ្មុំ',       0, false, NOW(), NOW(), 'system', 'system');

-- =====================================================================
-- STEP 3: INSERT 2,500 DISTRICTS (instant)
--         Code: province_code || 00..99
-- =====================================================================
INSERT INTO location_district_cbc (id, district_code, district_en, district_kh, province_code, version, is_deleted, created_at, updated_at, created_by, updated_by)
SELECT
    gen_random_uuid(),
    p.province_code || LPAD(d.n::text, 2, '0'),
    p.province_en || ' District ' || d.n,
    p.province_kh || ' ស្រុក ' || d.n,
    p.province_code,
    0, false, NOW(), NOW(), 'system', 'system'
FROM location_province_cbc p
CROSS JOIN generate_series(0, 99) AS d(n)
WHERE p.is_deleted = false;

-- =====================================================================
-- STEP 4: INSERT 250,000 COMMUNES (~few seconds)
--         Code: district_code || 00..99
-- =====================================================================
INSERT INTO location_commune_cbc (id, commune_code, commune_en, commune_kh, district_code, version, is_deleted, created_at, updated_at, created_by, updated_by)
SELECT
    gen_random_uuid(),
    d.district_code || LPAD(c.n::text, 2, '0'),
    d.district_en || ' Commune ' || c.n,
    d.district_kh || ' ឃុំ ' || c.n,
    d.district_code,
    0, false, NOW(), NOW(), 'system', 'system'
FROM location_district_cbc d
CROSS JOIN generate_series(0, 99) AS c(n)
WHERE d.is_deleted = false;

-- =====================================================================
-- STEP 5: INSERT 25,000,000 VILLAGES (batched per province)
--         Code: commune_code || 00..99
--         Each batch = ~1M rows (10,000 communes × 100 villages)
-- =====================================================================
DO $$
DECLARE
    p_code TEXT;
    batch_count INT := 0;
    total_count BIGINT := 0;
    row_count BIGINT;
BEGIN
    FOR p_code IN
        SELECT province_code FROM location_province_cbc WHERE is_deleted = false ORDER BY province_code
    LOOP
        batch_count := batch_count + 1;

        INSERT INTO location_village_cbc (id, village_code, village_en, village_kh, commune_code, version, is_deleted, created_at, updated_at, created_by, updated_by)
        SELECT
            gen_random_uuid(),
            c.commune_code || LPAD(v.n::text, 2, '0'),
            c.commune_en || ' Village ' || v.n,
            c.commune_kh || ' ភូមិ ' || v.n,
            c.commune_code,
            0, false, NOW(), NOW(), 'system', 'system'
        FROM location_commune_cbc c
        CROSS JOIN generate_series(0, 99) AS v(n)
        WHERE c.is_deleted = false
        AND c.district_code LIKE p_code || '%';

        GET DIAGNOSTICS row_count = ROW_COUNT;
        total_count := total_count + row_count;
        RAISE NOTICE '[%/25] Province % done — % rows (total: %)', batch_count, p_code, row_count, total_count;
    END LOOP;

    RAISE NOTICE 'ALL DONE — % total village rows inserted', total_count;
END $$;

-- =====================================================================
-- STEP 6: RECREATE ALL INDEXES (building once on full data is fast)
-- =====================================================================

-- District indexes
CREATE UNIQUE INDEX IF NOT EXISTS uk_district_code    ON location_district_cbc (district_code);
CREATE INDEX IF NOT EXISTS idx_district_code           ON location_district_cbc (district_code);
CREATE INDEX IF NOT EXISTS idx_district_province       ON location_district_cbc (province_code);
CREATE INDEX IF NOT EXISTS idx_district_deleted        ON location_district_cbc (is_deleted);

-- Commune indexes
CREATE UNIQUE INDEX IF NOT EXISTS uk_commune_code     ON location_commune_cbc (commune_code);
CREATE INDEX IF NOT EXISTS idx_commune_code            ON location_commune_cbc (commune_code);
CREATE INDEX IF NOT EXISTS idx_commune_district        ON location_commune_cbc (district_code);
CREATE INDEX IF NOT EXISTS idx_commune_deleted         ON location_commune_cbc (is_deleted);

-- Village indexes (25M rows — this step takes a bit)
CREATE UNIQUE INDEX IF NOT EXISTS uk_village_code     ON location_village_cbc (village_code);
CREATE INDEX IF NOT EXISTS idx_village_code            ON location_village_cbc (village_code);
CREATE INDEX IF NOT EXISTS idx_village_commune         ON location_village_cbc (commune_code);
CREATE INDEX IF NOT EXISTS idx_village_deleted         ON location_village_cbc (is_deleted);

-- =====================================================================
-- STEP 7: ANALYZE tables so the query planner has fresh stats
-- =====================================================================
ANALYZE location_province_cbc;
ANALYZE location_district_cbc;
ANALYZE location_commune_cbc;
ANALYZE location_village_cbc;

-- =====================================================================
-- STEP 8: VERIFICATION
-- =====================================================================
SELECT
    (SELECT COUNT(*) FROM location_province_cbc  WHERE is_deleted = false) AS provinces,
    (SELECT COUNT(*) FROM location_district_cbc  WHERE is_deleted = false) AS districts,
    (SELECT COUNT(*) FROM location_commune_cbc   WHERE is_deleted = false) AS communes,
    (SELECT COUNT(*) FROM location_village_cbc   WHERE is_deleted = false) AS villages;
