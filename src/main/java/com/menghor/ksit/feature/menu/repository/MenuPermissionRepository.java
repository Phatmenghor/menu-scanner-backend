package com.menghor.ksit.feature.menu.repository;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.menu.models.MenuPermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuPermissionRepository extends JpaRepository<MenuPermissionEntity, Long> {
    
    List<MenuPermissionEntity> findByRoleAndStatusOrderByDisplayOrder(RoleEnum role, Status status);
    
    List<MenuPermissionEntity> findByMenuItemIdAndStatus(Long menuItemId, Status status);
    
    Optional<MenuPermissionEntity> findByMenuItemIdAndRole(Long menuItemId, RoleEnum role);
    
    @Query("SELECT mp FROM MenuPermissionEntity mp WHERE mp.role = :role AND mp.canView = true AND mp.status = :status ORDER BY mp.displayOrder")
    List<MenuPermissionEntity> findViewableMenusByRole(@Param("role") RoleEnum role, @Param("status") Status status);
    
    @Query("SELECT mp FROM MenuPermissionEntity mp WHERE mp.role IN :roles AND mp.canView = true AND mp.status = :status ORDER BY mp.displayOrder")
    List<MenuPermissionEntity> findViewableMenusByRoles(@Param("roles") List<RoleEnum> roles, @Param("status") Status status);
}