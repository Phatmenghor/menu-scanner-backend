package com.emenu.features.user_management.domain;

import com.emenu.enums.*;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = true)
public class User extends BaseUUIDEntity {

    // Basic Information
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    // User Classification
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.PENDING;

    // Business Relationship
    @Column(name = "business_id")
    private UUID businessId;

    // Verification
    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    @Column(name = "email_verification_token")
    private String emailVerificationToken;

    @Column(name = "email_verification_expires")
    private LocalDateTime emailVerificationExpires;

    // Password Reset
    @Column(name = "password_reset_token")
    private String passwordResetToken;

    @Column(name = "password_reset_expires")
    private LocalDateTime passwordResetExpires;

    // Activity
    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "last_active")
    private LocalDateTime lastActive;

    // Customer Info (simplified)
    @Column(name = "loyalty_points")
    private Integer loyaltyPoints = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_tier")
    private CustomerTier customerTier = CustomerTier.BRONZE;

    // Roles Relationship
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles = new ArrayList<>();

    // Business Methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isActive() {
        return Status.ACTIVE.equals(status);
    }

    public boolean canLogin() {
        return Status.ACTIVE.equals(status) && Boolean.TRUE.equals(emailVerified);
    }

    public boolean hasRole(RoleEnum role) {
        return roles != null && roles.stream()
                .anyMatch(r -> r.getName().equals(role));
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

    public void updateActivity() {
        this.lastActive = LocalDateTime.now();
    }

    public void addLoyaltyPoints(int points) {
        this.loyaltyPoints = (this.loyaltyPoints == null ? 0 : this.loyaltyPoints) + points;
        this.customerTier = CustomerTier.fromPoints(this.loyaltyPoints);
    }
}