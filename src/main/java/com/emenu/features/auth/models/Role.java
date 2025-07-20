package com.emenu.features.auth.models;

import com.emenu.enums.RoleEnum;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Entity
@Table(name = "roles")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Role extends BaseUUIDEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true)
    private RoleEnum name;

    @Column(name = "description")
    private String description;

    @ManyToMany(mappedBy = "roles")
    private List<User> users;

    public Role(RoleEnum name) {
        this.name = name;
        this.description = name.getDescription();
    }

    public boolean hasPermission(String permission) {
        // Define role-based permissions with expanded role system
        Set<String> permissions = getRolePermissions(this.name);
        return permissions.contains(permission) || permissions.contains("*");
    }

    private Set<String> getRolePermissions(RoleEnum role) {
        return switch (role) {
            // Platform Roles - Hierarchical permissions
            case PLATFORM_OWNER -> Set.of("*"); // All permissions
            
            case PLATFORM_ADMIN -> Set.of(
                "user:*", "business:*", "customer:*", "message:*", "subscription:*", 
                "payment:*", "audit:read", "system:read", "platform:manage"
            );
            
            case PLATFORM_MANAGER -> Set.of(
                "user:read", "user:update", "business:read", "business:update", 
                "customer:read", "customer:update", "message:*", "subscription:read", 
                "payment:read", "audit:read"
            );
            
            case PLATFORM_DEVELOPER -> Set.of(
                "system:read", "audit:read", "user:read", "business:read", 
                "message:read", "subscription:read"
            );
            
            case PLATFORM_SALES -> Set.of(
                "business:read", "business:create", "customer:read", "subscription:*", 
                "payment:read", "message:create"
            );
            
            case PLATFORM_SUPPORT -> Set.of(
                "user:read", "user:update", "customer:read", "customer:update", 
                "message:*", "support:*"
            );

            // Business Roles
            case BUSINESS_OWNER -> Set.of(
                "business:read", "business:update", "staff:*", "customer:read", 
                "message:*", "subscription:read", "subscription:update", "payment:read"
            );
            
            case BUSINESS_MANAGER -> Set.of(
                "business:read", "staff:read", "staff:update", "customer:read", 
                "message:*", "subscription:read"
            );
            
            case BUSINESS_STAFF -> Set.of(
                "business:read", "customer:read", "message:read", "message:create"
            );

            // Customer Roles
            case VIP_CUSTOMER -> Set.of(
                "user:read", "user:update", "message:*", "loyalty:*", "order:*", 
                "vip:access", "priority:support"
            );
            
            case CUSTOMER -> Set.of(
                "user:read", "user:update", "message:read", "message:create", 
                "loyalty:read", "order:*"
            );
            
            case GUEST_CUSTOMER -> Set.of(
                "user:read", "order:read"
            );

            default -> Set.of();
        };
    }

    // Helper methods for role checking
    public boolean isPlatformRole() {
        return name.isPlatformRole();
    }

    public boolean isBusinessRole() {
        return name.isBusinessRole();
    }

    public boolean isCustomerRole() {
        return name.isCustomerRole();
    }

    public boolean hasHigherAuthorityThan(Role other) {
        return this.name.hasHigherAuthority(other.name);
    }
}
