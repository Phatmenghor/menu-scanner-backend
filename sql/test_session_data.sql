-- =============================================================================
-- Test Session Data Generation Script for pgAdmin
-- Generates 17 test sessions for ALL users in the system
-- =============================================================================

DO $$
DECLARE
    v_user RECORD;
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
    v_user_count INT := 0;
    v_total_sessions INT := 0;
    i INT;
BEGIN
    RAISE NOTICE '=============================================================================';
    RAISE NOTICE 'Starting test session generation for ALL users...';
    RAISE NOTICE '=============================================================================';

    -- Loop through all active users
    FOR v_user IN
        SELECT id, user_identifier, user_type
        FROM users
        WHERE is_deleted = FALSE
        ORDER BY created_at
    LOOP
        -- Check if sessions already exist for this user
        IF EXISTS (SELECT 1 FROM user_sessions WHERE user_id = v_user.id AND is_deleted = FALSE LIMIT 1) THEN
            RAISE NOTICE 'Skipping user % - sessions already exist', v_user.user_identifier;
            CONTINUE;
        END IF;

        v_user_count := v_user_count + 1;
        RAISE NOTICE '';
        RAISE NOTICE 'Creating 17 sessions for user #%: % (Type: %)', v_user_count, v_user.user_identifier, v_user.user_type;

        -- Create 17 sessions for this user
        FOR i IN 0..16 LOOP
            v_session_id := gen_random_uuid();
            v_device_type := v_device_types[(i % 4) + 1];
            v_browser := v_browsers[(i % 8) + 1];
            v_os := v_operating_systems[(i % 8) + 1];

            -- Vary login times based on user count to make data more realistic
            v_login_time := v_now - INTERVAL '30 days' + (i * INTERVAL '2 days') - (i * INTERVAL '1 hour') - (i * INTERVAL '5 minutes') - (v_user_count * INTERVAL '1 hour');

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
                v_user.id,
                gen_random_uuid()::TEXT,
                v_device_name,
                v_device_type,
                'Mozilla/5.0 (' || v_os || ') AppleWebKit/537.36 (KHTML, like Gecko) ' || v_browser || ' Safari/537.36',
                v_browser,
                v_os,
                v_ip_prefixes[(i % 8) + 1] || (100 + i + v_user_count),
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

            v_total_sessions := v_total_sessions + 1;
        END LOOP;

        -- Update user's active session count and login timestamps
        UPDATE users
        SET active_sessions_count = (
                SELECT COUNT(*) FROM user_sessions
                WHERE user_id = v_user.id AND status = 'ACTIVE' AND is_deleted = FALSE
            ),
            last_login_at = v_now,
            last_active_at = v_now,
            updated_at = v_now
        WHERE id = v_user.id;

        RAISE NOTICE '  -> Created 17 sessions (12 ACTIVE, 2 LOGGED_OUT, 2 EXPIRED, 1 REVOKED)';
    END LOOP;

    RAISE NOTICE '';
    RAISE NOTICE '=============================================================================';
    RAISE NOTICE 'COMPLETED! Created % sessions for % users', v_total_sessions, v_user_count;
    RAISE NOTICE '=============================================================================';
END $$;

-- =============================================================================
-- Verification Query - Check all users and their session counts
-- =============================================================================
SELECT
    u.user_identifier,
    u.user_type,
    u.active_sessions_count,
    COUNT(us.id) as total_sessions,
    SUM(CASE WHEN us.status = 'ACTIVE' THEN 1 ELSE 0 END) as active_sessions,
    SUM(CASE WHEN us.status = 'LOGGED_OUT' THEN 1 ELSE 0 END) as logged_out_sessions,
    SUM(CASE WHEN us.status = 'EXPIRED' THEN 1 ELSE 0 END) as expired_sessions,
    SUM(CASE WHEN us.status = 'REVOKED' THEN 1 ELSE 0 END) as revoked_sessions
FROM users u
LEFT JOIN user_sessions us ON u.id = us.user_id AND us.is_deleted = FALSE
WHERE u.is_deleted = FALSE
GROUP BY u.id, u.user_identifier, u.user_type, u.active_sessions_count
ORDER BY u.created_at;

-- =============================================================================
-- Summary Query - Total sessions by status across all users
-- =============================================================================
SELECT
    status,
    COUNT(*) as count
FROM user_sessions
WHERE is_deleted = FALSE
GROUP BY status
ORDER BY count DESC;

-- =============================================================================
-- Detailed Session View - All sessions with user info
-- =============================================================================
SELECT
    u.user_identifier,
    u.user_type,
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
WHERE us.is_deleted = FALSE
ORDER BY u.user_identifier, us.login_at DESC;
