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
 * RefreshToken entity for managing long-lived refresh tokens.
 * Refresh tokens are used to obtain new access tokens without re-authentication.
 *
 * @author Cambodia E-Menu Platform
 * @version 1.0.0
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_token", columnList = "token"),
        @Index(name = "idx_refresh_token_user", columnList = "user_id"),
        @Index(name = "idx_refresh_token_expiry", columnList = "expiry_date"),
        @Index(name = "idx_refresh_token_revoked", columnList = "is_revoked")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken extends BaseUUIDEntity {

    /**
     * The actual refresh token string (JWT)
     */
    @Column(name = "token", nullable = false, unique = true, columnDefinition = "TEXT")
    private String token;

    /**
     * Reference to the user who owns this refresh token
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * The user entity (lazy loaded)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /**
     * Expiration date of the refresh token
     */
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    /**
     * Flag indicating if the token has been revoked (e.g., on logout)
     */
    @Column(name = "is_revoked", nullable = false)
    private Boolean isRevoked = false;

    /**
     * Date when the token was revoked
     */
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    /**
     * Reason for token revocation (e.g., "logout", "password_change", "admin_action")
     */
    @Column(name = "revocation_reason")
    private String revocationReason;

    /**
     * Device or client information (optional, for security tracking)
     */
    @Column(name = "device_info")
    private String deviceInfo;

    /**
     * IP address from which the token was created
     */
    @Column(name = "ip_address")
    private String ipAddress;

    /**
     * Check if the refresh token is expired
     *
     * @return true if expired, false otherwise
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    /**
     * Check if the refresh token is valid (not expired and not revoked)
     *
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return !isExpired() && !isRevoked && !Boolean.TRUE.equals(getIsDeleted());
    }

    /**
     * Revoke the refresh token
     *
     * @param reason the reason for revocation
     */
    public void revoke(String reason) {
        this.isRevoked = true;
        this.revokedAt = LocalDateTime.now();
        this.revocationReason = reason;
    }
}
