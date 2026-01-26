package com.emenu.features.auth.models;

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
@Table(name = "roles", indexes = {
        @Index(name = "idx_role_deleted", columnList = "is_deleted"),
        @Index(name = "idx_role_name", columnList = "name, is_deleted"),
        @Index(name = "idx_role_business", columnList = "business_id, is_deleted"),
        @Index(name = "idx_role_user_type", columnList = "user_type, is_deleted")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_role_name_business", columnNames = {"name", "business_id"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Role extends BaseUUIDEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "display_name", length = 200)
    private String displayName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "business_id")
    private UUID businessId;

    /**
     * The user type this role belongs to.
     * PLATFORM_USER - for platform admin roles
     * BUSINESS_USER - for business-specific roles
     * CUSTOMER - for customer roles
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", length = 50)
    private UserType userType;

    @ManyToMany(mappedBy = "roles")
    private List<User> users;

    public boolean isPlatformOwner() {
        return "PLATFORM_OWNER".equals(name);
    }

    public boolean isBusinessOwner() {
        return "BUSINESS_OWNER".equals(name);
    }

    public boolean isCustomer() {
        return "CUSTOMER".equals(name);
    }

    public boolean isPlatformRole() {
        return businessId == null && (name != null && name.startsWith("PLATFORM_"));
    }

    public boolean isBusinessRole() {
        return businessId != null || (name != null && name.startsWith("BUSINESS_"));
    }

    public boolean isCustomerRole() {
        return name != null && name.startsWith("CUSTOMER");
    }
}