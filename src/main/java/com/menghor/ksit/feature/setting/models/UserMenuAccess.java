package com.menghor.ksit.feature.setting.models;

import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity to track which users can access which menu items
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "user_menu_access",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "menu_item_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMenuAccess extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @Column(nullable = false)
    private boolean hasAccess;
}