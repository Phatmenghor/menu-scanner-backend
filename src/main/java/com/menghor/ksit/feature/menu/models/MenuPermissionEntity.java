package com.menghor.ksit.feature.menu.models;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "menu_permissions", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"menu_item_id", "role_name", "user_id"},
                           name = "uk_menu_role_user")
       })
public class MenuPermissionEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItemEntity menuItem;

    // Role-based permission (default permissions)
    @Enumerated(EnumType.STRING)
    @Column(name = "role_name")
    private RoleEnum role;

    // User-specific permission (custom overrides)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "can_view", nullable = false)
    private Boolean canView = false;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    // Helper method to check if this is a role-based permission
    public boolean isRoleBasedPermission() {
        return role != null && user == null;
    }

    // Helper method to check if this is a user-specific permission
    public boolean isUserSpecificPermission() {
        return user != null;
    }
}