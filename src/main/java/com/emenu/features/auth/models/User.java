package com.emenu.features.auth.models;

import com.emenu.enums.auth.SocialProvider;
import com.emenu.enums.user.AccountStatus;
import com.emenu.enums.user.UserType;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_identifier", columnList = "userIdentifier"),
        @Index(name = "idx_telegram_user_id", columnList = "telegramUserId"),
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_account_status", columnList = "accountStatus"),
        @Index(name = "idx_business_id", columnList = "businessId")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseUUIDEntity {

    @Column(name = "user_identifier", nullable = false, unique = true)
    private String userIdentifier;

    @Column(name = "email")
    private String email;

    @Column(name = "password") // Nullable for social login users
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

    // ✅ NEW: Social Login Integration
    @Enumerated(EnumType.STRING)
    @Column(name = "social_provider", nullable = false)
    private SocialProvider socialProvider = SocialProvider.LOCAL;

    // ✅ NEW: Telegram Integration Fields
    @Column(name = "telegram_user_id", unique = true)
    private Long telegramUserId;

    @Column(name = "telegram_username")
    private String telegramUsername;

    @Column(name = "telegram_first_name")
    private String telegramFirstName;

    @Column(name = "telegram_last_name")
    private String telegramLastName;

    @Column(name = "telegram_linked_at")
    private LocalDateTime telegramLinkedAt;

    @Column(name = "telegram_notifications_enabled")
    private Boolean telegramNotificationsEnabled = true;

    @Column(name = "last_telegram_activity")
    private LocalDateTime lastTelegramActivity;

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

    public String getDisplayName() {
        String fullName = getFullName();
        if (!fullName.equals(userIdentifier)) {
            return fullName;
        }
        if (telegramFirstName != null) {
            return telegramFirstName + (telegramLastName != null ? " " + telegramLastName : "");
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

    public boolean hasBusinessAccess() {
        return businessId != null && (isBusinessUser() || isPlatformUser());
    }

    public boolean hasTelegramLinked() {
        return telegramUserId != null && telegramLinkedAt != null;
    }

    public boolean canReceiveTelegramNotifications() {
        return hasTelegramLinked() && Boolean.TRUE.equals(telegramNotificationsEnabled);
    }

    public boolean isSocialUser() {
        return socialProvider != null && socialProvider.isSocial();
    }

    public boolean requiresPassword() {
        return socialProvider == null || socialProvider.requiresPassword();
    }

    public void linkTelegram(Long telegramUserId, String telegramUsername,
                             String telegramFirstName, String telegramLastName) {
        this.telegramUserId = telegramUserId;
        this.telegramUsername = telegramUsername;
        this.telegramFirstName = telegramFirstName;
        this.telegramLastName = telegramLastName;
        this.telegramLinkedAt = LocalDateTime.now();
        this.lastTelegramActivity = LocalDateTime.now();

        // If this was a Telegram-created user, update social provider
        if (this.socialProvider == SocialProvider.LOCAL && this.password == null) {
            this.socialProvider = SocialProvider.TELEGRAM;
        }
    }

    public void unlinkTelegram() {
        this.telegramUserId = null;
        this.telegramUsername = null;
        this.telegramFirstName = null;
        this.telegramLastName = null;
        this.telegramLinkedAt = null;
        this.telegramNotificationsEnabled = true;
        this.lastTelegramActivity = null;
    }

    public void updateTelegramActivity() {
        this.lastTelegramActivity = LocalDateTime.now();
    }

    public String getTelegramDisplayName() {
        if (telegramUsername != null) {
            return "@" + telegramUsername;
        }
        if (telegramFirstName != null) {
            return telegramFirstName + (telegramLastName != null ? " " + telegramLastName : "");
        }
        return telegramUserId != null ? "User " + telegramUserId : "Unknown";
    }
}