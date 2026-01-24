package com.emenu.features.auth.models;

import com.emenu.enums.user.RoleEnum;
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
    @Column(name = "name")
    private RoleEnum name;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "display_name", length = 200)
    private String displayName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "business_id")
    private UUID businessId;

    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = false;

    @ManyToMany(mappedBy = "roles")
    private List<User> users;

    public Role(RoleEnum name) {
        this.name = name;
        this.code = name.name();
        this.displayName = name.getDisplayName();
        this.description = name.getDescription();
        this.businessId = null;
        this.isSystem = true;
    }

    public boolean isPlatformRole() {
        return businessId == null && RoleEnum.PLATFORM_OWNER.equals(name);
    }

    public boolean isBusinessRole() {
        return businessId != null || RoleEnum.BUSINESS_OWNER.equals(name);
    }

    public boolean isCustomerRole() {
        return RoleEnum.CUSTOMER.equals(name);
    }
}