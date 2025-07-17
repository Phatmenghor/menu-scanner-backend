package com.emenu.features.usermanagement.domain;

import com.emenu.enums.*;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = true)
public class User extends BaseUUIDEntity {

    @Column(name = "email", unique = true, nullable = false, length = 255)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private GenderEnum gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus = AccountStatus.PENDING_VERIFICATION;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "timezone", length = 50)
    private String timezone = "UTC";

    @Column(name = "language", length = 10)
    private String language = "en";

    // Authentication related fields
    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    @Column(name = "email_verification_token")
    private String emailVerificationToken;

    @Column(name = "email_verification_expires")
    private LocalDateTime emailVerificationExpires;

    @Column(name = "password_reset_token")
    private String passwordResetToken;

    @Column(name = "password_reset_expires")
    private LocalDateTime passwordResetExpires;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "login_attempts")
    private Integer loginAttempts = 0;

    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;

    @Column(name = "two_factor_enabled")
    private Boolean twoFactorEnabled = false;

    @Column(name = "two_factor_secret")
    private String twoFactorSecret;

    // Customer specific fields
    @Enumerated(EnumType.STRING)
    @Column(name = "customer_tier")
    private CustomerTier customerTier = CustomerTier.BRONZE;

    @Column(name = "loyalty_points")
    private Integer loyaltyPoints = 0;

    @Column(name = "total_orders")
    private Integer totalOrders = 0;

    @Column(name = "total_spent", precision = 10, scale = 2)
    private Double totalSpent = 0.0;

    // Business relationship
    @Column(name = "business_id")
    private UUID businessId;

    // Notification preferences
    @Column(name = "email_notifications")
    private Boolean emailNotifications = true;

    @Column(name = "telegram_notifications")
    private Boolean telegramNotifications = false;

    @Column(name = "telegram_user_id")
    private String telegramUserId;

    @Column(name = "telegram_chat_id")
    private String telegramChatId;

    @Column(name = "marketing_emails")
    private Boolean marketingEmails = false;

    @Column(name = "order_notifications")
    private Boolean orderNotifications = true;

    @Column(name = "loyalty_notifications")
    private Boolean loyaltyNotifications = true;

    // Roles
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<Role> roles = new ArrayList<>();

    // Business logic methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isActive() {
        return AccountStatus.ACTIVE.equals(accountStatus) && !getIsDeleted();
    }

    public boolean isLocked() {
        return AccountStatus.LOCKED.equals(accountStatus) || 
               (accountLockedUntil != null && LocalDateTime.now().isBefore(accountLockedUntil));
    }

    public boolean isEmailVerified() {
        return Boolean.TRUE.equals(emailVerified);
    }

    public void incrementLoginAttempts() {
        this.loginAttempts = (this.loginAttempts == null ? 0 : this.loginAttempts) + 1;
        if (this.loginAttempts >= 5) {
            this.accountLockedUntil = LocalDateTime.now().plusMinutes(30);
            this.accountStatus = AccountStatus.LOCKED;
        }
    }

    public void resetLoginAttempts() {
        this.loginAttempts = 0;
        this.accountLockedUntil = null;
        this.lastLogin = LocalDateTime.now();
        if (AccountStatus.LOCKED.equals(this.accountStatus)) {
            this.accountStatus = AccountStatus.ACTIVE;
        }
    }

    public void addLoyaltyPoints(int points) {
        this.loyaltyPoints = (this.loyaltyPoints == null ? 0 : this.loyaltyPoints) + points;
        this.customerTier = CustomerTier.fromPoints(this.loyaltyPoints);
    }

    public void incrementTotalOrders() {
        this.totalOrders = (this.totalOrders == null ? 0 : this.totalOrders) + 1;
    }

    public void addToTotalSpent(double amount) {
        this.totalSpent = (this.totalSpent == null ? 0.0 : this.totalSpent) + amount;
    }

    public boolean hasRole(RoleEnum role) {
        return roles.stream().anyMatch(r -> r.getName().equals(role));
    }

    public boolean canReceiveEmailNotifications() {
        return Boolean.TRUE.equals(emailNotifications) && isEmailVerified();
    }

    public boolean canReceiveTelegramNotifications() {
        return Boolean.TRUE.equals(telegramNotifications) && telegramUserId != null;
    }
}