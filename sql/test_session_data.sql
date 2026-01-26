-- =============================================================================
-- Test Session Data Generation Script for pgAdmin
-- Generates 17 test sessions for the platform owner (user 1)
-- =============================================================================

-- First, get the platform owner's user ID
-- Replace this with the actual UUID if you know it, or use the query below
DO $$
DECLARE
    v_user_id UUID;
    v_now TIMESTAMP := NOW();
    v_device_types TEXT[] := ARRAY['WEB', 'MOBILE', 'TABLET', 'DESKTOP'];
    v_browsers TEXT[] := ARRAY['Chrome 120', 'Firefox 121', 'Safari 17', 'Edge 120', 'Chrome Mobile 120', 'Safari Mobile 17', 'Samsung Browser 23', 'Opera 106'];
    v_operating_systems TEXT[] := ARRAY['Windows 11', 'macOS Sonoma', 'Ubuntu 22.04', 'Android 14', 'iOS 17', 'Windows 10', 'macOS Ventura', 'Fedora 39'];
    v_cities TEXT[] := ARRAY['Phnom Penh', 'Siem Reap', 'Battambang', 'Sihanoukville', 'Kampot', 'Kep', 'Kratie', 'Banlung'];
    v_countries TEXT[] := ARRAY['Cambodia', 'Cambodia', 'Cambodia', 'Cambodia', 'Thailand', 'Vietnam', 'Singapore', 'Malaysia'];
    v_ip_prefixes TEXT[] := ARRAY['192.168.1.', '10.0.0.', '172.16.0.', '203.189.140.', '175.100.12.', '202.62.23.', '43.252.89.', '103.16.128.'];
    v_web_names TEXT[] := ARRAY['Office Workstation', 'Home Desktop', 'Work Laptop', 'Personal MacBook', 'Development PC'];
    v_mobile_names TEXT[] := ARRAY['iPhone 15 Pro', 'Samsung Galaxy S24', 'Google Pixel 8', 'OnePlus 12', 'Xiaomi 14'];
    v_tablet_names TEXT[] := ARRAY['iPad Pro 12.9', 'Samsung Tab S9', 'iPad Air', 'Lenovo Tab P12', 'Surface Pro 9'];
    v_desktop_names TEXT[] := ARRAY['Gaming PC', 'iMac 24', 'Mac Mini M2', 'Dell XPS Desktop', 'HP Pavilion'];
    v_logout_reasons TEXT[] := ARRAY['User initiated logout', 'Session timeout', 'Logged out from another device', 'Password changed', 'Security check logout'];

    v_device_type TEXT;
    v_device_name TEXT;
    v_browser TEXT;
    v_os TEXT;
    v_login_time TIMESTAMP;
    v_status TEXT;
    v_session_id UUID;
    i INT;
BEGIN
    -- Get the platform owner user ID (change email if needed)
    SELECT id INTO v_user_id
    FROM users
    WHERE user_identifier = 'phatmenghor19@gmail.com'
      AND is_deleted = FALSE
    LIMIT 1;

    IF v_user_id IS NULL THEN
        RAISE NOTICE 'Platform owner not found. Please check the user_identifier.';
        RETURN;
    END IF;

    -- Check if sessions already exist
    IF EXISTS (SELECT 1 FROM user_sessions WHERE user_id = v_user_id AND is_deleted = FALSE LIMIT 1) THEN
        RAISE NOTICE 'Test sessions already exist for this user. Skipping...';
        RETURN;
    END IF;

    RAISE NOTICE 'Creating 17 test sessions for user: %', v_user_id;

    -- Create 17 sessions
    FOR i IN 0..16 LOOP
        v_session_id := gen_random_uuid();
        v_device_type := v_device_types[(i % 4) + 1];
        v_browser := v_browsers[(i % 8) + 1];
        v_os := v_operating_systems[(i % 8) + 1];
        v_login_time := v_now - INTERVAL '30 days' + (i * INTERVAL '2 days') - (i * INTERVAL '1 hour') - (i * INTERVAL '5 minutes');

        -- Determine device name based on device type
        CASE v_device_type
            WHEN 'WEB' THEN v_device_name := v_web_names[(i % 5) + 1];
            WHEN 'MOBILE' THEN v_device_name := v_mobile_names[(i % 5) + 1];
            WHEN 'TABLET' THEN v_device_name := v_tablet_names[(i % 5) + 1];
            WHEN 'DESKTOP' THEN v_device_name := v_desktop_names[(i % 5) + 1];
            ELSE v_device_name := 'Unknown Device ' || (i + 1);
        END CASE;

        -- Determine session status (12 ACTIVE, 2 LOGGED_OUT, 2 EXPIRED, 1 REVOKED)
        IF i < 12 THEN
            v_status := 'ACTIVE';
        ELSIF i < 14 THEN
            v_status := 'LOGGED_OUT';
        ELSIF i < 16 THEN
            v_status := 'EXPIRED';
        ELSE
            v_status := 'REVOKED';
        END IF;

        -- Insert the session
        INSERT INTO user_sessions (
            id,
            user_id,
            device_id,
            device_name,
            device_type,
            user_agent,
            browser,
            operating_system,
            ip_address,
            city,
            country,
            status,
            login_at,
            last_active_at,
            expires_at,
            logged_out_at,
            logout_reason,
            is_current_session,
            is_deleted,
            created_at,
            updated_at
        ) VALUES (
            v_session_id,
            v_user_id,
            gen_random_uuid()::TEXT,
            v_device_name,
            v_device_type,
            'Mozilla/5.0 (' || v_os || ') AppleWebKit/537.36 (KHTML, like Gecko) ' || v_browser || ' Safari/537.36',
            v_browser,
            v_os,
            v_ip_prefixes[(i % 8) + 1] || (100 + i),
            v_cities[(i % 8) + 1],
            v_countries[(i % 8) + 1],
            v_status,
            v_login_time,
            v_login_time + (i % 12) * INTERVAL '1 hour' + (i * 3) * INTERVAL '1 minute',
            v_login_time + INTERVAL '7 days',
            CASE
                WHEN v_status IN ('LOGGED_OUT', 'EXPIRED', 'REVOKED') THEN v_login_time + INTERVAL '1 day' + (i % 6) * INTERVAL '1 hour'
                ELSE NULL
            END,
            CASE
                WHEN v_status = 'LOGGED_OUT' THEN v_logout_reasons[(i % 5) + 1]
                WHEN v_status = 'REVOKED' THEN 'Admin revocation for security audit'
                ELSE NULL
            END,
            CASE WHEN i = 0 AND v_status = 'ACTIVE' THEN TRUE ELSE FALSE END,
            FALSE,
            v_now,
            v_now
        );

        RAISE NOTICE 'Created session % - Status: %, Device: %', i + 1, v_status, v_device_name;
    END LOOP;

    -- Update user's active session count
    UPDATE users
    SET active_sessions_count = (
            SELECT COUNT(*) FROM user_sessions
            WHERE user_id = v_user_id AND status = 'ACTIVE' AND is_deleted = FALSE
        ),
        last_login_at = v_now,
        last_active_at = v_now,
        updated_at = v_now
    WHERE id = v_user_id;

    RAISE NOTICE 'Successfully created 17 test sessions for platform owner!';
END $$;

-- =============================================================================
-- Verification Query - Run this to verify the sessions were created
-- =============================================================================
SELECT
    us.id,
    us.device_name,
    us.device_type,
    us.browser,
    us.operating_system,
    us.ip_address,
    us.city,
    us.country,
    us.status,
    us.login_at,
    us.last_active_at,
    us.is_current_session
FROM user_sessions us
JOIN users u ON us.user_id = u.id
WHERE u.user_identifier = 'phatmenghor19@gmail.com'
  AND us.is_deleted = FALSE
ORDER BY us.login_at DESC;

-- =============================================================================
-- Summary Query - Check session counts by status
-- =============================================================================
SELECT
    status,
    COUNT(*) as count
FROM user_sessions us
JOIN users u ON us.user_id = u.id
WHERE u.user_identifier = 'phatmenghor19@gmail.com'
  AND us.is_deleted = FALSE
GROUP BY status
ORDER BY count DESC;
