package com.menghor.ksit.feature.menu.models;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "user_menu_permissions",
        indexes = {
                @Index(name = "idx_user_menu_perm_user_menu", columnList = "user_id, menu_item_id"),
                @Index(name = "idx_user_menu_perm_user", columnList = "user_id"),
                @Index(name = "idx_user_menu_perm_menu", columnList = "menu_item_id"),
                @Index(name = "idx_user_menu_perm_status", columnList = "status")
        })
@Data
public class UserMenuPermissionEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_menu_permission_user"))
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_menu_permission_menu_item"))
    private MenuItemEntity menuItem;

    @Column(name = "can_view", nullable = false)
    private Boolean canView = true;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0; // Custom order for this user

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @Column(name = "custom_title")
    private String customTitle; // Optional custom title for this user

    @Column(name = "custom_icon")
    private String customIcon; // Optional custom icon for this user
}
