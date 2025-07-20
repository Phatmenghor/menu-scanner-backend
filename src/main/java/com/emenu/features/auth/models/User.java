package com.emenu.features.auth.models;

import com.emenu.enums.AccountStatus;
import com.emenu.enums.CustomerTier;
import com.emenu.enums.UserType;
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
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseUUIDEntity {

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

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

    // Customer specific fields
    @Enumerated(EnumType.STRING)
    @Column(name = "customer_tier")
    private CustomerTier customerTier = CustomerTier.CUSTOMER;

    @Column(name = "loyalty_points")
    private Integer loyaltyPoints = 0;

    // Security fields
    @Column(name = "last_login_ip")
    private String lastLoginIp;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    @Column(name = "phone_verified")
    private Boolean phoneVerified = false;

    // Methods
    public String getFullName() {
        return firstName + " " + lastName;
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

    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts = (this.failedLoginAttempts == null ? 0 : this.failedLoginAttempts) + 1;
        
        // Lock account after 5 failed attempts for 30 minutes
        if (this.failedLoginAttempts >= 5) {
            this.accountStatus = AccountStatus.LOCKED;
            this.lockedUntil = LocalDateTime.now().plusMinutes(30);
        }
    }

    public boolean isAccountLocked() {
        if (AccountStatus.LOCKED.equals(accountStatus)) {
            if (lockedUntil != null && lockedUntil.isBefore(LocalDateTime.now())) {
                // Auto-unlock if lock period has expired
                this.accountStatus = AccountStatus.ACTIVE;
                this.lockedUntil = null;
                this.failedLoginAttempts = 0;
                return false;
            }
            return true;
        }
        return false;
    }
}