package com.emenu.features.auth.models;

import com.emenu.enums.user.AccountStatus;
import com.emenu.enums.user.UserType;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_deleted", columnList = "is_deleted"),
        @Index(name = "idx_user_identifier", columnList = "user_identifier, is_deleted"),
        @Index(name = "idx_user_business", columnList = "business_id, is_deleted"),
        @Index(name = "idx_user_identifier_type", columnList = "user_identifier, user_type, is_deleted"),
        @Index(name = "idx_user_identifier_business", columnList = "user_identifier, business_id, is_deleted")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_platform_user_identifier", columnNames = {"user_identifier", "user_type"}),
        @UniqueConstraint(name = "uk_business_user_identifier", columnNames = {"user_identifier", "business_id"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseUUIDEntity {

    @Column(name = "user_identifier", nullable = false)
    private String userIdentifier;

    @Column(name = "email")
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    @Column(name = "business_id")
    private UUID businessId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", insertable = false, updatable = false)
    private Business business;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles;

    @Column(name = "position")
    private String position;

    @Column(name = "address")
    private String address;

    @Column(name = "notes")
    private String notes;

    // Telegram integration fields
    @Column(name = "telegram_id", unique = true)
    private Long telegramId;

    @Column(name = "telegram_username")
    private String telegramUsername;

    @Column(name = "telegram_first_name")
    private String telegramFirstName;

    @Column(name = "telegram_last_name")
    private String telegramLastName;

    @Column(name = "telegram_synced_at")
    private java.time.LocalDateTime telegramSyncedAt;

    // Google OAuth integration fields
    @Column(name = "google_id")
    private String googleId;

    @Column(name = "google_email")
    private String googleEmail;

    @Column(name = "google_synced_at")
    private java.time.LocalDateTime googleSyncedAt;

    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return userIdentifier;
    }

    public boolean isActive() {
        return AccountStatus.ACTIVE.equals(accountStatus);
    }

    public boolean isPlatformUser() {
        return UserType.PLATFORM_USER.equals(userType);
    }

    public boolean isBusinessUser() {
        return UserType.BUSINESS_USER.equals(userType);
    }

    public boolean isCustomer() {
        return UserType.CUSTOMER.equals(userType);
    }

    public boolean hasTelegramSynced() {
        return telegramId != null;
    }

    public boolean hasGoogleSynced() {
        return googleId != null && googleEmail != null;
    }

    public void syncTelegram(Long telegramId, String telegramUsername, String telegramFirstName, String telegramLastName) {
        this.telegramId = telegramId;
        this.telegramUsername = telegramUsername;
        this.telegramFirstName = telegramFirstName;
        this.telegramLastName = telegramLastName;
        this.telegramSyncedAt = java.time.LocalDateTime.now();
    }

    public void unsyncTelegram() {
        this.telegramId = null;
        this.telegramUsername = null;
        this.telegramFirstName = null;
        this.telegramLastName = null;
        this.telegramSyncedAt = null;
    }

    public void syncGoogle(String googleId, String googleEmail) {
        this.googleId = googleId;
        this.googleEmail = googleEmail;
        this.googleSyncedAt = java.time.LocalDateTime.now();
    }

    public void unsyncGoogle() {
        this.googleId = null;
        this.googleEmail = null;
        this.googleSyncedAt = null;
    }
}
