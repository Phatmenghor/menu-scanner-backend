package com.menghor.ksit.feature.menu.repository;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.menu.models.MenuPermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuPermissionRepository extends JpaRepository<MenuPermissionEntity, Long> {

    /**
     * Find viewable menus by user roles (role-based permissions)
     */
    @Query("SELECT mp FROM MenuPermissionEntity mp " +
           "WHERE mp.role IN :roles " +
           "AND mp.status = :status " +
           "AND mp.canView = true " +
           "AND mp.user IS NULL " +
           "ORDER BY mp.displayOrder ASC")
    List<MenuPermissionEntity> findViewableMenusByRoles(@Param("roles") List<RoleEnum> roles,
                                                        @Param("status") Status status);

    /**
     * Find all role-based permissions for specific roles
     */
    @Query("SELECT mp FROM MenuPermissionEntity mp " +
           "WHERE mp.role IN :roles " +
           "AND mp.status = :status " +
           "AND mp.user IS NULL " +
           "ORDER BY mp.displayOrder ASC")
    List<MenuPermissionEntity> findByRolesAndStatus(@Param("roles") List<RoleEnum> roles, 
                                                    @Param("status") Status status);

    /**
     * Find user-specific permissions
     */
    @Query("SELECT mp FROM MenuPermissionEntity mp " +
           "WHERE mp.user.id = :userId " +
           "AND mp.status = :status " +
           "ORDER BY mp.displayOrder ASC")
    List<MenuPermissionEntity> findByUserIdAndStatus(@Param("userId") Long userId, 
                                                     @Param("status") Status status);

    /**
     * Find specific user permission for a menu item
     */
    @Query("SELECT mp FROM MenuPermissionEntity mp " +
           "WHERE mp.user.id = :userId " +
           "AND mp.menuItem.id = :menuItemId " +
           "AND mp.status = :status")
    Optional<MenuPermissionEntity> findByUserIdAndMenuItemIdAndStatus(@Param("userId") Long userId,
                                                                      @Param("menuItemId") Long menuItemId,
                                                                      @Param("status") Status status);

    /**
     * Check if user has custom permission for a menu item
     */
    @Query("SELECT COUNT(mp) > 0 FROM MenuPermissionEntity mp " +
           "WHERE mp.user.id = :userId " +
           "AND mp.menuItem.id = :menuItemId " +
           "AND mp.status = :status")
    boolean existsByUserIdAndMenuItemIdAndStatus(@Param("userId") Long userId,
                                                @Param("menuItemId") Long menuItemId,
                                                @Param("status") Status status);

    /**
     * Find all permissions for a specific menu item
     */
    @Query("SELECT mp FROM MenuPermissionEntity mp " +
           "WHERE mp.menuItem.id = :menuItemId " +
           "AND mp.status = :status " +
           "ORDER BY mp.displayOrder ASC")
    List<MenuPermissionEntity> findByMenuItemIdAndStatus(@Param("menuItemId") Long menuItemId,
                                                         @Param("status") Status status);

    /**
     * Find role-based permission for specific menu and role
     */
    @Query("SELECT mp FROM MenuPermissionEntity mp " +
           "WHERE mp.menuItem.id = :menuItemId " +
           "AND mp.role = :role " +
           "AND mp.status = :status " +
           "AND mp.user IS NULL")
    Optional<MenuPermissionEntity> findByMenuItemIdAndRoleAndStatus(@Param("menuItemId") Long menuItemId,
                                                                   @Param("role") RoleEnum role,
                                                                   @Param("status") Status status);

    /**
     * Soft delete all user-specific permissions for a user (for reset functionality)
     */
    @Modifying
    @Query("UPDATE MenuPermissionEntity mp SET mp.status = 'DELETED' " +
           "WHERE mp.user.id = :userId AND mp.status = 'ACTIVE'")
    void softDeleteUserPermissions(@Param("userId") Long userId);
}