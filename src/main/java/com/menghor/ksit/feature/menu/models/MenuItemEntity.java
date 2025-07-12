package com.menghor.ksit.feature.menu.models;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "menu_items",
        indexes = {
                @Index(name = "idx_menu_item_code", columnList = "code"),
                @Index(name = "idx_menu_item_parent", columnList = "parent_id"),
                @Index(name = "idx_menu_item_status", columnList = "status"),
                @Index(name = "idx_menu_item_display_order", columnList = "display_order")
        })
@Data
public class MenuItemEntity extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code; // Unique identifier like "DASHBOARD", "MASTER_DATA", etc.

    @Column(nullable = false)
    private String title; // Display name
    private String route; // URL route (null for parent items)
    private String icon; // Icon identifier

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0; // For ordering menu items

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @Column(name = "is_parent", nullable = false)
    private Boolean isParent = false; // True if this is a parent menu item

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(name = "fk_menu_item_parent"))
    private MenuItemEntity parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MenuItemEntity> children = new ArrayList<>();

    @OneToMany(mappedBy = "menuItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MenuPermissionEntity> permissions = new ArrayList<>();

    @OneToMany(mappedBy = "menuItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserMenuPermissionEntity> userPermissions = new ArrayList<>();
}