package com.emenu.features.auth.models;

import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_sessions", indexes = {
        @Index(name = "idx_session_user", columnList = "user_id, is_deleted"),
        @Index(name = "idx_session_status", columnList = "status, is_deleted"),
        @Index(name = "idx_session_device", columnList = "device_id, is_deleted"),
        @Index(name = "idx_session_token", columnList = "refresh_token_id, is_deleted")
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

    // Device Information
    @Column(name = "device_id", length = 255)
    private String deviceId;

    @Column(name = "device_name", length = 255)
    private String deviceName;

    @Column(name = "device_type", length = 50)
    private String deviceType;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "browser", length = 100)
    private String browser;

    @Column(name = "operating_system", length = 100)
    private String operatingSystem;

    // Location Information
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "location", length = 255)
    private String location;

    // Session Status
    @Column(name = "status", length = 50, nullable = false)
    private String status;

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
        if (loginAt == null) loginAt = LocalDateTime.now();
        if (lastActiveAt == null) lastActiveAt = LocalDateTime.now();
        if (status == null) status = "ACTIVE";
    }

    public boolean isActive() {
        return "ACTIVE".equals(status) && (expiresAt == null || expiresAt.isAfter(LocalDateTime.now()));
    }

    public void logout(String reason) {
        this.status = "LOGGED_OUT";
        this.loggedOutAt = LocalDateTime.now();
        this.logoutReason = reason;
        this.isCurrentSession = false;
    }

    public void expire() {
        this.status = "EXPIRED";
        this.loggedOutAt = LocalDateTime.now();
        this.isCurrentSession = false;
    }

    public String getDeviceDisplayName() {
        if (deviceName != null && !deviceName.isEmpty()) return deviceName;
        if (browser != null && operatingSystem != null) return browser + " on " + operatingSystem;
        if (deviceType != null) return deviceType;
        return "Unknown Device";
    }
}
