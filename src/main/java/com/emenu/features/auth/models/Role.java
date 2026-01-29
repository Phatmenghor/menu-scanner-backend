package com.emenu.features.auth.models;

import com.emenu.enums.user.UserType;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "roles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_role_name_business", columnNames = {"name", "business_id"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Role extends BaseUUIDEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "business_id")
    private UUID businessId;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", length = 50)
    private UserType userType;

    public boolean isCustomer() {
        return "CUSTOMER".equals(name);
    }

    /**
     * Check if this role is compatible with the given user type
     */
    public boolean isCompatibleWithUserType(UserType targetUserType) {
        if (userType == null || targetUserType == null) {
            return true; // Allow null for backward compatibility
        }
        return userType == targetUserType;
    }
}