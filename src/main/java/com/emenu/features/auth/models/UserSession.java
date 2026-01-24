package com.emenu.features.auth.models;

import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Tracks user login sessions and devices for multi-device management
 */
@Entity
@Table(name = "user_sessions", indexes = {
        @Index(name = "idx_session_user", columnList = "user_id, is_deleted"),
        @Index(name = "idx_session_status", columnList = "status, is_deleted"),
        @Index(name = "idx_session_device", columnList = "device_id, is_deleted"),
        @Index(name = "idx_session_token", columnList = "refresh_token_id, is_deleted"),
        @Index(name = "idx_session_active", columnList = "user_id, status, last_active_at"),
        @Index(name = "idx_session_expires", columnList = "expires_at, status")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserSession extends BaseUUIDEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "refresh_token_id")
    private UUID refreshTokenId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refresh_token_id", insertable = false, updatable = false)
    private RefreshToken refreshToken;

    // Device Information
    @Column(name = "device_id", length = 255)
    private String deviceId;

    @Column(name = "device_name", length = 255)
    private String deviceName;

    @Column(name = "device_type", length = 50)
    private String deviceType; // WEB, MOBILE, TABLET, DESKTOP

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "browser", length = 100)
    private String browser;

    @Column(name = "operating_system", length = 100)
    private String operatingSystem;

    // Location Information
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "city", length = 100)
    private String city;

    // Session Status
    @Column(name = "status", length = 50, nullable = false)
    private String status; // ACTIVE, EXPIRED, REVOKED, LOGGED_OUT

    // Timestamps
    @Column(name = "login_at", nullable = false)
    private LocalDateTime loginAt;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "logged_out_at")
    private LocalDateTime loggedOutAt;

    // Metadata
    @Column(name = "is_current_session")
    private Boolean isCurrentSession = false;

    @Column(name = "logout_reason", length = 500)
    private String logoutReason;

    @PrePersist
    public void prePersist() {
        super.prePersist();
        if (loginAt == null) {
            loginAt = LocalDateTime.now();
        }
        if (lastActiveAt == null) {
            lastActiveAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "ACTIVE";
        }
    }

    /**
     * Check if session is active
     */
    public boolean isActive() {
        return "ACTIVE".equals(status) &&
               (expiresAt == null || expiresAt.isAfter(LocalDateTime.now()));
    }

    /**
     * Check if session is expired
     */
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    /**
     * Update last active timestamp
     */
    public void updateLastActive() {
        this.lastActiveAt = LocalDateTime.now();
    }

    /**
     * Mark session as logged out
     */
    public void logout(String reason) {
        this.status = "LOGGED_OUT";
        this.loggedOutAt = LocalDateTime.now();
        this.logoutReason = reason;
        this.isCurrentSession = false;
    }

    /**
     * Revoke session
     */
    public void revoke(String reason) {
        this.status = "REVOKED";
        this.logoutReason = reason;
        this.isCurrentSession = false;
    }

    /**
     * Mark session as expired
     */
    public void expire() {
        this.status = "EXPIRED";
        this.isCurrentSession = false;
    }

    /**
     * Get session duration in minutes
     */
    public long getSessionDurationMinutes() {
        LocalDateTime endTime = loggedOutAt != null ? loggedOutAt : LocalDateTime.now();
        return java.time.Duration.between(loginAt, endTime).toMinutes();
    }

    /**
     * Get inactive duration in minutes
     */
    public long getInactiveDurationMinutes() {
        if (lastActiveAt == null) {
            return 0;
        }
        return java.time.Duration.between(lastActiveAt, LocalDateTime.now()).toMinutes();
    }

    /**
     * Get device display name
     */
    public String getDeviceDisplayName() {
        if (deviceName != null && !deviceName.isEmpty()) {
            return deviceName;
        }
        if (browser != null && operatingSystem != null) {
            return browser + " on " + operatingSystem;
        }
        if (deviceType != null) {
            return deviceType;
        }
        return "Unknown Device";
    }
}
