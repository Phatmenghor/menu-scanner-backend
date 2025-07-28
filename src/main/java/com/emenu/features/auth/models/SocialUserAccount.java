package com.emenu.features.auth.models;

import com.emenu.enums.auth.SocialProvider;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "social_user_accounts", indexes = {
    @Index(name = "idx_social_provider_id", columnList = "provider, providerId", unique = true),
    @Index(name = "idx_user_id", columnList = "userId"),
    @Index(name = "idx_provider", columnList = "provider")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SocialUserAccount extends BaseUUIDEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private SocialProvider provider;

    @Column(name = "provider_id", nullable = false)
    private String providerId; // Google: sub, Telegram: id

    @Column(name = "provider_username")
    private String providerUsername; // Telegram: username, Google: email

    @Column(name = "provider_email")
    private String providerEmail; // Google: email, Telegram: can be null

    @Column(name = "provider_name")
    private String providerName; // Full name from provider

    @Column(name = "provider_picture_url")
    private String providerPictureUrl; // Profile picture URL

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false; // Primary account for login

    @Column(name = "provider_data", columnDefinition = "TEXT")
    private String providerData; // JSON data from provider

    // Helper methods
    public boolean isGoogle() {
        return SocialProvider.GOOGLE.equals(provider);
    }

    public boolean isTelegram() {
        return SocialProvider.TELEGRAM.equals(provider);
    }

    public void markAsUsed() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public String getDisplayName() {
        if (providerName != null && !providerName.trim().isEmpty()) {
            return providerName;
        }
        if (providerUsername != null && !providerUsername.trim().isEmpty()) {
            return providerUsername;
        }
        if (providerEmail != null && !providerEmail.trim().isEmpty()) {
            return providerEmail;
        }
        return "User from " + provider.getDisplayName();
    }
}