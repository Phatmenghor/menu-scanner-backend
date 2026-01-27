-- =====================================================================
-- E-Menu Platform: Cambodia Location Data Generation
-- Generates: 25 Provinces, 2,500 Districts, 250,000 Communes, 25,000,000 Villages
-- Run in pgAdmin (may take several minutes for 25M village rows)
-- Safe to re-run (skips existing records by code).
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
--    Code format: PPDD (e.g., 0101 = Province 01, District 01)
-- =====================================================================
INSERT INTO location_district_cbc (id, district_code, district_en, district_kh, province_code, version, is_deleted, created_at, updated_at, created_by, updated_by)
SELECT
    gen_random_uuid(),
    LPAD(p.pnum::text, 2, '0') || LPAD(d.dnum::text, 2, '0'),
    p.province_en || ' District ' || d.dnum,
    p.province_kh || ' ស្រុក ' || d.dnum,
    LPAD(p.pnum::text, 2, '0'),
    0,
    false,
    NOW(),
    NOW(),
    'system',
    'system'
FROM (
    SELECT province_en, province_kh, ROW_NUMBER() OVER (ORDER BY province_code) AS pnum, province_code
    FROM location_province_cbc WHERE is_deleted = false
) p
CROSS JOIN generate_series(1, 100) AS d(dnum)
WHERE NOT EXISTS (
    SELECT 1 FROM location_district_cbc dc
    WHERE dc.district_code = LPAD(p.pnum::text, 2, '0') || LPAD(d.dnum::text, 2, '0')
);

-- =====================================================================
-- 3. INSERT 250,000 COMMUNES (100 per district)
--    Code format: PPDDCC (e.g., 010101 = Province 01, District 01, Commune 01)
-- =====================================================================
INSERT INTO location_commune_cbc (id, commune_code, commune_en, commune_kh, district_code, version, is_deleted, created_at, updated_at, created_by, updated_by)
SELECT
    gen_random_uuid(),
    dc.district_code || LPAD(c.cnum::text, 2, '0'),
    dc.district_en || ' Commune ' || c.cnum,
    dc.district_kh || ' ឃុំ ' || c.cnum,
    dc.district_code,
    0,
    false,
    NOW(),
    NOW(),
    'system',
    'system'
FROM location_district_cbc dc
CROSS JOIN generate_series(1, 100) AS c(cnum)
WHERE dc.is_deleted = false
AND NOT EXISTS (
    SELECT 1 FROM location_commune_cbc cc
    WHERE cc.commune_code = dc.district_code || LPAD(c.cnum::text, 2, '0')
);

-- =====================================================================
-- 4. INSERT 25,000,000 VILLAGES (100 per commune)
--    Code format: PPDDCCVV (e.g., 01010101 = Province 01, District 01, Commune 01, Village 01)
--    NOTE: This inserts 25 million rows. It may take several minutes.
--    Consider running in batches if needed (see batch version below).
-- =====================================================================
INSERT INTO location_village_cbc (id, village_code, village_en, village_kh, commune_code, version, is_deleted, created_at, updated_at, created_by, updated_by)
SELECT
    gen_random_uuid(),
    cc.commune_code || LPAD(v.vnum::text, 2, '0'),
    cc.commune_en || ' Village ' || v.vnum,
    cc.commune_kh || ' ភូមិ ' || v.vnum,
    cc.commune_code,
    0,
    false,
    NOW(),
    NOW(),
    'system',
    'system'
FROM location_commune_cbc cc
CROSS JOIN generate_series(1, 100) AS v(vnum)
WHERE cc.is_deleted = false
AND NOT EXISTS (
    SELECT 1 FROM location_village_cbc vc
    WHERE vc.village_code = cc.commune_code || LPAD(v.vnum::text, 2, '0')
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
-- Uncomment and run one province at a time.
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
--             cc.commune_code || LPAD(v.vnum::text, 2, '0'),
--             cc.commune_en || ' Village ' || v.vnum,
--             cc.commune_kh || ' ភូមិ ' || v.vnum,
--             cc.commune_code,
--             0,
--             false,
--             NOW(),
--             NOW(),
--             'system',
--             'system'
--         FROM location_commune_cbc cc
--         CROSS JOIN generate_series(1, 100) AS v(vnum)
--         WHERE cc.is_deleted = false
--         AND cc.district_code LIKE p_code || '%'
--         AND NOT EXISTS (
--             SELECT 1 FROM location_village_cbc vc
--             WHERE vc.village_code = cc.commune_code || LPAD(v.vnum::text, 2, '0')
--         );
--
--         RAISE NOTICE 'Completed villages for province %', p_code;
--     END LOOP;
-- END $$;
