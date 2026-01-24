package com.emenu.features.auth.models;

import com.emenu.enums.user.RoleEnum;
import com.emenu.enums.user.RoleScope;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "roles", indexes = {
        @Index(name = "idx_role_deleted", columnList = "is_deleted"),
        @Index(name = "idx_role_name", columnList = "name, is_deleted"),
        @Index(name = "idx_role_code", columnList = "code"),
        @Index(name = "idx_role_scope", columnList = "scope, is_deleted"),
        @Index(name = "idx_role_business", columnList = "business_id, is_deleted")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_role_code_business", columnNames = {"code", "business_id"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Role extends BaseUUIDEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false)
    private RoleEnum name;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "display_name", length = 200)
    private String displayName;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false)
    private RoleScope scope;

    @Column(name = "business_id")
    private UUID businessId;

    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = false;

    @ManyToMany(mappedBy = "roles")
    private List<User> users;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private List<Permission> permissions;

    public Role(RoleEnum name) {
        this.name = name;
        this.code = name.name();
        this.displayName = name.getDescription();
        this.description = name.getDescription();
        this.scope = determineScope(name);
        this.isSystem = true;
    }

    private RoleScope determineScope(RoleEnum roleEnum) {
        return switch (roleEnum) {
            case PLATFORM_OWNER, PLATFORM_ADMIN, PLATFORM_STAFF -> RoleScope.PLATFORM;
            case BUSINESS_OWNER, BUSINESS_ADMIN, BUSINESS_STAFF -> RoleScope.BUSINESS;
            case CUSTOMER -> RoleScope.CUSTOMER;
        };
    }

    public boolean isPlatformRole() {
        return RoleScope.PLATFORM.equals(scope);
    }

    public boolean isBusinessRole() {
        return RoleScope.BUSINESS.equals(scope);
    }

    public boolean isCustomerRole() {
        return RoleScope.CUSTOMER.equals(scope);
    }
}