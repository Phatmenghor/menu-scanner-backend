package com.emenu.features.user_management.models;

import com.emenu.enums.AccountStatus;
import com.emenu.enums.CustomerTier;
import com.emenu.enums.UserType;
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

    // Business relationship
    @Column(name = "business_id")
    private UUID businessId;

    // Roles
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles;

    // Customer fields
    @Enumerated(EnumType.STRING)
    @Column(name = "customer_tier")
    private CustomerTier customerTier = CustomerTier.BRONZE;

    @Column(name = "loyalty_points")
    private Integer loyaltyPoints = 0;

    // Employee fields
    @Column(name = "position")
    private String position;

    @Column(name = "salary")
    private Double salary;

    // Simple contact info
    @Column(name = "address")
    private String address;

    @Column(name = "notes")
    private String notes;

    // Convenience methods
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
        return businessId != null;
    }
}