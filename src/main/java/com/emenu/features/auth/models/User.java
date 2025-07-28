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
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseUUIDEntity {

    // ✅ NEW: User identifier for login (can be anything)
    @Column(name = "user_identifier", nullable = false, unique = true)
    private String userIdentifier;

    @Column(name = "email")
    private String email;

    @Column(name = "password") // ✅ UPDATED: Made nullable for social-only accounts
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

    // ✅ NEW: Social accounts relationship
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SocialUserAccount> socialAccounts;

    @Column(name = "position")
    private String position;

    @Column(name = "address")
    private String address;

    @Column(name = "notes")
    private String notes;

    // ✅ NEW: Social login related fields
    @Column(name = "has_password")
    private Boolean hasPassword = true;

    @Column(name = "social_only_account")
    private Boolean socialOnlyAccount = false;

    // Methods
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return userIdentifier; // Fallback to userIdentifier
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

    // ✅ NEW: Social login helper methods
    public boolean hasSocialAccounts() {
        return socialAccounts != null && !socialAccounts.isEmpty();
    }

    public boolean canLoginWithPassword() {
        return hasPassword != null && hasPassword && password != null && !password.trim().isEmpty();
    }

    public boolean canLoginSocially() {
        return hasSocialAccounts();
    }

    public boolean isSocialOnlyAccount() {
        return Boolean.TRUE.equals(socialOnlyAccount) || !canLoginWithPassword();
    }

    public void setSocialOnlyAccount() {
        this.socialOnlyAccount = true;
        this.hasPassword = false;
        this.password = null;
    }

    public void enablePasswordLogin(String encodedPassword) {
        this.password = encodedPassword;
        this.hasPassword = true;
        this.socialOnlyAccount = false;
    }

    // ✅ NEW: Get primary social account
    public SocialUserAccount getPrimarySocialAccount() {
        if (socialAccounts == null || socialAccounts.isEmpty()) {
            return null;
        }

        return socialAccounts.stream()
                .filter(account -> Boolean.TRUE.equals(account.getIsPrimary()))
                .findFirst()
                .orElse(socialAccounts.get(0)); // Fallback to first account
    }

    // ✅ NEW: Update profile from social account
    public void updateProfileFromSocialAccount(SocialUserAccount socialAccount) {
        // Update profile picture if not set or if from social account
        if (profileImageUrl == null || profileImageUrl.trim().isEmpty()) {
            if (socialAccount.getProviderPictureUrl() != null) {
                this.profileImageUrl = socialAccount.getProviderPictureUrl();
            }
        }

        // Update name if not set
        if ((firstName == null || firstName.trim().isEmpty()) &&
                socialAccount.getProviderName() != null) {
            String[] nameParts = socialAccount.getProviderName().trim().split("\\s+", 2);
            this.firstName = nameParts[0];
            if (nameParts.length > 1) {
                this.lastName = nameParts[1];
            }
        }

        // Update email if not set (Google accounts)
        if ((email == null || email.trim().isEmpty()) &&
                socialAccount.getProviderEmail() != null) {
            this.email = socialAccount.getProviderEmail();
        }
    }

    // ✅ NEW: Check if user has specific social provider
    public boolean hasSocialProvider(com.emenu.enums.auth.SocialProvider provider) {
        if (socialAccounts == null) return false;

        return socialAccounts.stream()
                .anyMatch(account -> account.getProvider() == provider && !account.getIsDeleted());
    }
}