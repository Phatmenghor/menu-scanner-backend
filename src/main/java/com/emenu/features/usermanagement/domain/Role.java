package com.emenu.features.usermanagement.domain;

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

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_system_role")
    private Boolean isSystemRole = true;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id")
    )
    @Column(name = "permission")
    private List<String> permissions = new ArrayList<>();

    public Role() {}

    public Role(RoleEnum name) {
        this.name = name;
        this.description = name.getDescription();
        this.isSystemRole = true;
        this.permissions = getDefaultPermissionsForRole(name);
    }

    public Role(RoleEnum name, String description) {
        this.name = name;
        this.description = description;
        this.isSystemRole = true;
        this.permissions = getDefaultPermissionsForRole(name);
    }

    private List<String> getDefaultPermissionsForRole(RoleEnum role) {
        List<String> perms = new ArrayList<>();
        
        switch (role) {
            case PLATFORM_OWNER:
                perms.addAll(List.of("*:*")); // All permissions
                break;
            case PLATFORM_MANAGER:
                perms.addAll(List.of(
                    "users:read", "users:create", "users:update", "users:delete",
                    "businesses:read", "businesses:update", "businesses:approve",
                    "subscriptions:read", "subscriptions:manage",
                    "analytics:read", "reports:read"
                ));
                break;
            case PLATFORM_STAFF:
                perms.addAll(List.of(
                    "users:read", "businesses:read", "support:handle"
                ));
                break;
            case PLATFORM_DEVELOPER:
                perms.addAll(List.of(
                    "system:read", "logs:read", "api:manage"
                ));
                break;
            case PLATFORM_SUPPORT:
                perms.addAll(List.of(
                    "users:read", "businesses:read", "support:handle", "tickets:manage"
                ));
                break;
            case PLATFORM_SALES:
                perms.addAll(List.of(
                    "leads:read", "leads:manage", "subscriptions:read", "sales:manage"
                ));
                break;
            case BUSINESS_OWNER:
                perms.addAll(List.of(
                    "business:manage", "staff:manage", "menu:manage", 
                    "orders:read", "analytics:business", "customers:read"
                ));
                break;
            case BUSINESS_MANAGER:
                perms.addAll(List.of(
                    "business:read", "staff:read", "menu:update", 
                    "orders:manage", "customers:read"
                ));
                break;
            case BUSINESS_STAFF:
                perms.addAll(List.of(
                    "orders:read", "menu:read", "customers:basic"
                ));
                break;
            case CUSTOMER:
            case VIP_CUSTOMER:
            case GUEST_CUSTOMER:
                perms.addAll(List.of(
                    "profile:read", "profile:update", "orders:own", "reviews:create"
                ));
                break;
        }
        
        return perms;
    }

    public boolean hasPermission(String permission) {
        if (permissions.contains("*:*")) return true;
        return permissions.contains(permission);
    }
}