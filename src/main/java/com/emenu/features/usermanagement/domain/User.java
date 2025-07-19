package com.emenu.features.usermanagement.domain;

import com.emenu.enums.*;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_phone", columnList = "phone_number"),
        @Index(name = "idx_user_business_id", columnList = "business_id"),
        @Index(name = "idx_user_user_type", columnList = "user_type"),
        @Index(name = "idx_user_account_status", columnList = "account_status"),
        @Index(name = "idx_user_customer_tier", columnList = "customer_tier"),
        @Index(name = "idx_user_created_at", columnList = "created_at"),
        @Index(name = "idx_user_last_login", columnList = "last_login")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"roles", "businesses", "subscriptions"})
@ToString(exclude = {"roles", "businesses", "subscriptions", "password", "twoFactorSecret"})
public class User extends BaseUUIDEntity {

    // Basic Information
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

    // User Classification
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus = AccountStatus.PENDING_VERIFICATION;

    // Profile Information
    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "company", length = 200)
    private String company;

    @Column(name = "position", length = 100)
    private String position;

    // Address Information
    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    // Preferences
    @Column(name = "timezone", length = 50)
    private String timezone = "UTC";

    @Column(name = "language", length = 10)
    private String language = "en";

    @Column(name = "currency", length = 10)
    private String currency = "USD";

    // Authentication & Security
    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    @Column(name = "phone_verified")
    private Boolean phoneVerified = false;

    @Column(name = "email_verification_token")
    private String emailVerificationToken;

    @Column(name = "email_verification_expires")
    private LocalDateTime emailVerificationExpires;

    @Column(name = "phone_verification_token")
    private String phoneVerificationToken;

    @Column(name = "phone_verification_expires")
    private LocalDateTime phoneVerificationExpires;

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

    @Column(name = "backup_codes", columnDefinition = "TEXT")
    private String backupCodes;

    // Customer Specific Fields
    @Enumerated(EnumType.STRING)
    @Column(name = "customer_tier")
    private CustomerTier customerTier = CustomerTier.BRONZE;

    @Column(name = "loyalty_points")
    private Integer loyaltyPoints = 0;

    @Column(name = "total_orders")
    private Integer totalOrders = 0;

    @Column(name = "total_spent", precision = 12, scale = 2)
    private Double totalSpent = 0.0;

    @Column(name = "favorite_cuisines", columnDefinition = "TEXT")
    private String favoriteCuisines; // JSON array

    @Column(name = "dietary_restrictions", columnDefinition = "TEXT")
    private String dietaryRestrictions; // JSON array

    // Business Relationships
    @Column(name = "business_id")
    private UUID businessId; // For business staff - which business they work for

    @Column(name = "primary_business_id")
    private UUID primaryBusinessId; // For business owners - their main business

    @ElementCollection
    @CollectionTable(
            name = "user_business_access",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "business_id")
    private List<UUID> accessibleBusinessIds = new ArrayList<>(); // Businesses user can access

    // Platform Information (for platform users)
    @Column(name = "employee_id", length = 50)
    private String employeeId;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "salary", precision = 12, scale = 2)
    private Double salary;

    @Column(name = "commission_rate", precision = 5, scale = 4)
    private Double commissionRate; // For sales staff

    // Subscription & Payment (for business owners)
    @Column(name = "subscription_id")
    private UUID subscriptionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan")
    private SubscriptionPlan subscriptionPlan;

    @Column(name = "subscription_starts")
    private LocalDateTime subscriptionStarts;

    @Column(name = "subscription_ends")
    private LocalDateTime subscriptionEnds;

    @Column(name = "payment_method_id")
    private String paymentMethodId; // Stripe customer ID or similar

    @Column(name = "billing_address", columnDefinition = "TEXT")
    private String billingAddress; // JSON

    // Notification Preferences
    @Column(name = "email_notifications")
    private Boolean emailNotifications = true;

    @Column(name = "telegram_notifications")
    private Boolean telegramNotifications = false;

    @Column(name = "telegram_user_id")
    private String telegramUserId;

    @Column(name = "telegram_chat_id")
    private String telegramChatId;

    @Column(name = "telegram_username")
    private String telegramUsername;

    @Column(name = "marketing_emails")
    private Boolean marketingEmails = false;

    @Column(name = "order_notifications")
    private Boolean orderNotifications = true;

    @Column(name = "loyalty_notifications")
    private Boolean loyaltyNotifications = true;

    @Column(name = "platform_notifications")
    private Boolean platformNotifications = true;

    @Column(name = "security_notifications")
    private Boolean securityNotifications = true;

    // Activity Tracking
    @Column(name = "last_active")
    private LocalDateTime lastActive;

    @Column(name = "session_count")
    private Integer sessionCount = 0;

    @Column(name = "total_login_time", columnDefinition = "BIGINT DEFAULT 0")
    private Long totalLoginTime = 0L; // in minutes

    @Column(name = "last_password_change")
    private LocalDateTime lastPasswordChange;

    @Column(name = "password_change_required")
    private Boolean passwordChangeRequired = false;

    // Marketing & Analytics
    @Column(name = "referral_code", length = 20)
    private String referralCode;

    @Column(name = "referred_by_user_id")
    private UUID referredByUserId;

    @Column(name = "utm_source", length = 100)
    private String utmSource;

    @Column(name = "utm_medium", length = 100)
    private String utmMedium;

    @Column(name = "utm_campaign", length = 100)
    private String utmCampaign;

    @Column(name = "registration_ip", length = 45)
    private String registrationIp;

    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    // Terms & Compliance
    @Column(name = "terms_accepted")
    private Boolean termsAccepted = false;

    @Column(name = "terms_accepted_at")
    private LocalDateTime termsAcceptedAt;

    @Column(name = "privacy_accepted")
    private Boolean privacyAccepted = false;

    @Column(name = "privacy_accepted_at")
    private LocalDateTime privacyAcceptedAt;

    @Column(name = "data_processing_consent")
    private Boolean dataProcessingConsent = false;

    @Column(name = "marketing_consent")
    private Boolean marketingConsent = false;

    // Roles Relationship
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles = new ArrayList<>();

    // Business Logic Methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getDisplayName() {
        if (company != null && !company.trim().isEmpty()) {
            return getFullName() + " (" + company + ")";
        }
        return getFullName();
    }

    public boolean isActive() {
        return accountStatus != null && accountStatus.isActive() && !getIsDeleted();
    }

    public boolean canLogin() {
        return accountStatus != null && accountStatus.canLogin() && !isLocked() && !getIsDeleted();
    }

    public boolean isLocked() {
        return AccountStatus.LOCKED.equals(accountStatus) ||
                (accountLockedUntil != null && LocalDateTime.now().isBefore(accountLockedUntil));
    }

    public boolean isEmailVerified() {
        return Boolean.TRUE.equals(emailVerified);
    }

    public boolean isPhoneVerified() {
        return Boolean.TRUE.equals(phoneVerified);
    }

    public boolean isTwoFactorEnabled() {
        return Boolean.TRUE.equals(twoFactorEnabled) && twoFactorSecret != null;
    }

    public void incrementLoginAttempts() {
        this.loginAttempts = (this.loginAttempts == null ? 0 : this.loginAttempts) + 1;

        // Lock account after 5 failed attempts
        if (this.loginAttempts >= 5) {
            this.accountLockedUntil = LocalDateTime.now().plusMinutes(30);
            this.accountStatus = AccountStatus.LOCKED;
        }
    }

    public void resetLoginAttempts() {
        this.loginAttempts = 0;
        this.accountLockedUntil = null;
        this.lastLogin = LocalDateTime.now();
        this.lastActive = LocalDateTime.now();
        this.sessionCount = (this.sessionCount == null ? 0 : this.sessionCount) + 1;

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
        return roles != null && roles.stream().anyMatch(r -> r.getName().equals(role));
    }

    public boolean hasAnyRole(RoleEnum... roles) {
        if (this.roles == null || this.roles.isEmpty()) return false;
        for (RoleEnum role : roles) {
            if (hasRole(role)) return true;
        }
        return false;
    }

    public boolean isPlatformUser() {
        return UserType.PLATFORM_USER.equals(userType) ||
                (roles != null && roles.stream().anyMatch(r -> r.getName().isPlatformRole()));
    }

    public boolean isBusinessUser() {
        return UserType.BUSINESS_USER.equals(userType) ||
                (roles != null && roles.stream().anyMatch(r -> r.getName().isBusinessRole()));
    }

    public boolean isCustomer() {
        return UserType.CUSTOMER.equals(userType) || UserType.GUEST.equals(userType) ||
                (roles != null && roles.stream().anyMatch(r -> r.getName().isCustomerRole()));
    }

    public boolean canReceiveEmailNotifications() {
        return Boolean.TRUE.equals(emailNotifications) && isEmailVerified();
    }

    public boolean canReceiveTelegramNotifications() {
        return Boolean.TRUE.equals(telegramNotifications) && telegramUserId != null;
    }

    public boolean canAccessBusiness(UUID businessId) {
        if (isPlatformUser()) return true; // Platform users can access all businesses

        return businessId.equals(this.businessId) ||
                businessId.equals(this.primaryBusinessId) ||
                (accessibleBusinessIds != null && accessibleBusinessIds.contains(businessId));
    }

    public void grantBusinessAccess(UUID businessId) {
        if (accessibleBusinessIds == null) {
            accessibleBusinessIds = new ArrayList<>();
        }
        if (!accessibleBusinessIds.contains(businessId)) {
            accessibleBusinessIds.add(businessId);
        }
    }

    public void revokeBusinessAccess(UUID businessId) {
        if (accessibleBusinessIds != null) {
            accessibleBusinessIds.remove(businessId);
        }
    }

    public boolean hasActiveSubscription() {
        return subscriptionEnds != null && subscriptionEnds.isAfter(LocalDateTime.now()) &&
                subscriptionPlan != null && subscriptionPlan != SubscriptionPlan.FREE;
    }

    public boolean isSubscriptionExpired() {
        return subscriptionEnds != null && subscriptionEnds.isBefore(LocalDateTime.now());
    }

    public long getDaysUntilSubscriptionExpires() {
        if (subscriptionEnds == null) return -1;
        return java.time.Duration.between(LocalDateTime.now(), subscriptionEnds).toDays();
    }

    public void updateActivity() {
        this.lastActive = LocalDateTime.now();
    }

    public void acceptTermsAndPrivacy() {
        this.termsAccepted = true;
        this.termsAcceptedAt = LocalDateTime.now();
        this.privacyAccepted = true;
        this.privacyAcceptedAt = LocalDateTime.now();
    }

    // Pre-persist and pre-update callbacks
    @PrePersist
    private void prePersist() {
        if (referralCode == null) {
            referralCode = generateReferralCode();
        }
        if (registrationIp == null) {
            registrationIp = getCurrentIp();
        }
    }

    private String generateReferralCode() {
        return "REF" + System.currentTimeMillis() % 100000;
    }

    private String getCurrentIp() {
        // This would be implemented to get the current request IP
        return "0.0.0.0";
    }
}