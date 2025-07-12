package com.menghor.ksit.feature.menu.models;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "menu_permissions",
        indexes = {
                @Index(name = "idx_menu_perm_menu_role", columnList = "menu_item_id, role"),
                @Index(name = "idx_menu_perm_role", columnList = "role"),
                @Index(name = "idx_menu_perm_status", columnList = "status")
        })
@Data
public class MenuPermissionEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_menu_permission_menu_item"))
    private MenuItemEntity menuItem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleEnum role;

    @Column(name = "can_view", nullable = false)
    private Boolean canView = true;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0; // Custom order for this role

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

}