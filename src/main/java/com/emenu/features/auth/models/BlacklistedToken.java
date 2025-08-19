package com.emenu.features.auth.models;

import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "blacklisted_tokens", indexes = {
        // ✅ EXISTING: Keep critical security indexes
        @Index(name = "idx_token_hash", columnList = "tokenHash"),
        @Index(name = "idx_expires_at", columnList = "expiresAt"),
        @Index(name = "idx_user_email", columnList = "userEmail"),

        // ✅ FIXED: BaseUUIDEntity indexes with unique names
        @Index(name = "idx_blacklisted_token_deleted", columnList = "is_deleted"),
        @Index(name = "idx_blacklisted_token_deleted_created", columnList = "is_deleted, created_at"),
        @Index(name = "idx_blacklisted_token_expires_deleted", columnList = "expires_at, is_deleted")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistedToken extends BaseUUIDEntity {

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash; // SHA-256 hash of the token for security

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "reason")
    private String reason; // LOGOUT, ADMIN_REVOKE, etc.

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "ip_address")
    private String ipAddress;

    // Constructor for creating blacklisted token
    public BlacklistedToken(String tokenHash, String userEmail, LocalDateTime expiresAt, String reason) {
        this.tokenHash = tokenHash;
        this.userEmail = userEmail;
        this.expiresAt = expiresAt;
        this.reason = reason;
    }

    // Check if token is expired (can be cleaned up)
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}