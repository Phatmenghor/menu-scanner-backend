package com.emenu.features.user_management.domain;

import com.emenu.enums.RoleEnum;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roles")
@Data
@EqualsAndHashCode(callSuper = true)
public class Role extends BaseUUIDEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "name", unique = true, nullable = false)
    private RoleEnum name;

    @Column(name = "description")
    private String description;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"))
    @Column(name = "permission")
    private List<String> permissions = new ArrayList<>();

    public Role() {}

    public Role(RoleEnum name) {
        this.name = name;
        this.description = name.getDescription();
        this.permissions = getDefaultPermissions(name);
    }

    private List<String> getDefaultPermissions(RoleEnum role) {
        return switch (role) {
            case PLATFORM_OWNER -> List.of("*:*");
            case PLATFORM_ADMIN -> List.of("users:*", "businesses:*", "subscriptions:*");
            case PLATFORM_SUPPORT -> List.of("users:read", "businesses:read", "support:*");
            case BUSINESS_OWNER -> List.of("business:*", "staff:*", "customers:read");
            case BUSINESS_MANAGER -> List.of("business:read", "staff:read", "customers:read");
            case BUSINESS_STAFF -> List.of("orders:*", "customers:basic");
            case CUSTOMER, VIP_CUSTOMER -> List.of("profile:*", "orders:own");
        };
    }

    public boolean hasPermission(String permission) {
        return permissions.contains("*:*") || permissions.contains(permission);
    }
}