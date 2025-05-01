package com.menghor.ksit.feature.setting.models;

import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

/**
 * Entity to represent a UI menu item in the dashboard sidebar
 */
@Entity
@Table(name = "dashboard_menu_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String menuKey;

    @Column(nullable = false)
    private String menuName;

    private String parentKey;

    @Column(nullable = false)
    private boolean parent;

    private String icon;

    private String route;

    private Integer displayOrder;

    @Column(nullable = false)
    private boolean enabled;
}