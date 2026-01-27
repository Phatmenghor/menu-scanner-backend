-- =====================================================================
-- E-Menu Platform: Cambodia Location Data Generation
-- Generates: 25 Provinces, 2,500 Districts, 250,000 Communes, 25,000,000 Villages
-- Run in pgAdmin (may take several minutes for 25M village rows)
-- Safe to re-run (skips existing records by code).
--
-- Code format (each level appends 2 digits from parent):
--   Province: PP           → 01, 02, ... 25
--   District: PP+DD        → 0100, 0101, ... 0199
--   Commune:  PPDD+CC      → 010000, 010001, ... 010099
--   Village:  PPDDCC+VV    → 01000000, 01000001, ... 01000099
-- =====================================================================

-- =====================================================================
-- 0. DROP OLD CHECK CONSTRAINTS (if Hibernate generated them from enums)
-- =====================================================================
ALTER TABLE location_province_cbc DROP CONSTRAINT IF EXISTS location_province_cbc_province_code_check;
ALTER TABLE location_district_cbc DROP CONSTRAINT IF EXISTS location_district_cbc_district_code_check;
ALTER TABLE location_commune_cbc DROP CONSTRAINT IF EXISTS location_commune_cbc_commune_code_check;
ALTER TABLE location_village_cbc DROP CONSTRAINT IF EXISTS location_village_cbc_village_code_check;

-- =====================================================================
-- 1. INSERT 25 PROVINCES (Real Cambodia provinces)
--    Code: 01 to 25
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
    (gen_random_uuid(), '25', 'Tboung Khmum',       'ត្បូងឃ្មុំ',       0, false, NOW(), NOW(), 'system', 'system')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- 2. INSERT 2,500 DISTRICTS (100 per province)
--    Code: province_code + 00..99  →  0100, 0101, ... 2599
-- =====================================================================
INSERT INTO location_district_cbc (id, district_code, district_en, district_kh, province_code, version, is_deleted, created_at, updated_at, created_by, updated_by)
SELECT
    gen_random_uuid(),
    p.province_code || LPAD(d.n::text, 2, '0'),
    p.province_en || ' District ' || d.n,
    p.province_kh || ' ស្រុក ' || d.n,
    p.province_code,
    0,
    false,
    NOW(), NOW(), 'system', 'system'
FROM location_province_cbc p
CROSS JOIN generate_series(0, 99) AS d(n)
WHERE p.is_deleted = false
AND NOT EXISTS (
    SELECT 1 FROM location_district_cbc ex
    WHERE ex.district_code = p.province_code || LPAD(d.n::text, 2, '0')
);

-- =====================================================================
-- 3. INSERT 250,000 COMMUNES (100 per district)
--    Code: district_code + 00..99  →  010000, 010001, ... 259999
-- =====================================================================
INSERT INTO location_commune_cbc (id, commune_code, commune_en, commune_kh, district_code, version, is_deleted, created_at, updated_at, created_by, updated_by)
SELECT
    gen_random_uuid(),
    d.district_code || LPAD(c.n::text, 2, '0'),
    d.district_en || ' Commune ' || c.n,
    d.district_kh || ' ឃុំ ' || c.n,
    d.district_code,
    0,
    false,
    NOW(), NOW(), 'system', 'system'
FROM location_district_cbc d
CROSS JOIN generate_series(0, 99) AS c(n)
WHERE d.is_deleted = false
AND NOT EXISTS (
    SELECT 1 FROM location_commune_cbc ex
    WHERE ex.commune_code = d.district_code || LPAD(c.n::text, 2, '0')
);

-- =====================================================================
-- 4. INSERT 25,000,000 VILLAGES (100 per commune)
--    Code: commune_code + 00..99  →  01000000, 01000001, ... 25999999
--    NOTE: 25 million rows — may take several minutes.
--    If it times out, use the BATCH version at the bottom instead.
-- =====================================================================
INSERT INTO location_village_cbc (id, village_code, village_en, village_kh, commune_code, version, is_deleted, created_at, updated_at, created_by, updated_by)
SELECT
    gen_random_uuid(),
    c.commune_code || LPAD(v.n::text, 2, '0'),
    c.commune_en || ' Village ' || v.n,
    c.commune_kh || ' ភូមិ ' || v.n,
    c.commune_code,
    0,
    false,
    NOW(), NOW(), 'system', 'system'
FROM location_commune_cbc c
CROSS JOIN generate_series(0, 99) AS v(n)
WHERE c.is_deleted = false
AND NOT EXISTS (
    SELECT 1 FROM location_village_cbc ex
    WHERE ex.village_code = c.commune_code || LPAD(v.n::text, 2, '0')
);

-- =====================================================================
-- 5. VERIFICATION
-- =====================================================================
SELECT
    (SELECT COUNT(*) FROM location_province_cbc  WHERE is_deleted = false) AS provinces,
    (SELECT COUNT(*) FROM location_district_cbc  WHERE is_deleted = false) AS districts,
    (SELECT COUNT(*) FROM location_commune_cbc   WHERE is_deleted = false) AS communes,
    (SELECT COUNT(*) FROM location_village_cbc   WHERE is_deleted = false) AS villages;

-- =====================================================================
-- ALTERNATIVE: BATCH INSERT VILLAGES (if the full 25M insert times out)
-- Uncomment and run — it inserts per province (~1M rows at a time).
-- =====================================================================
-- DO $$
-- DECLARE
--     p_code TEXT;
-- BEGIN
--     FOR p_code IN
--         SELECT province_code FROM location_province_cbc WHERE is_deleted = false ORDER BY province_code
--     LOOP
--         RAISE NOTICE 'Inserting villages for province %', p_code;
--
--         INSERT INTO location_village_cbc (id, village_code, village_en, village_kh, commune_code, version, is_deleted, created_at, updated_at, created_by, updated_by)
--         SELECT
--             gen_random_uuid(),
--             c.commune_code || LPAD(v.n::text, 2, '0'),
--             c.commune_en || ' Village ' || v.n,
--             c.commune_kh || ' ភូមិ ' || v.n,
--             c.commune_code,
--             0,
--             false,
--             NOW(), NOW(), 'system', 'system'
--         FROM location_commune_cbc c
--         CROSS JOIN generate_series(0, 99) AS v(n)
--         WHERE c.is_deleted = false
--         AND c.district_code LIKE p_code || '%'
--         AND NOT EXISTS (
--             SELECT 1 FROM location_village_cbc ex
--             WHERE ex.village_code = c.commune_code || LPAD(v.n::text, 2, '0')
--         );
--
--         RAISE NOTICE 'Done province %', p_code;
--     END LOOP;
-- END $$;
