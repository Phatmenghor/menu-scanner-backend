package com.menghor.ksit.feature.menu.repository;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.menu.models.UserMenuPermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMenuPermissionRepository extends JpaRepository<UserMenuPermissionEntity, Long> {
    
    List<UserMenuPermissionEntity> findByUserIdAndStatusOrderByDisplayOrder(Long userId, Status status);
    
    Optional<UserMenuPermissionEntity> findByUserIdAndMenuItemId(Long userId, Long menuItemId);
    
    @Query("SELECT ump FROM UserMenuPermissionEntity ump WHERE ump.user.id = :userId AND ump.canView = true AND ump.status = :status ORDER BY ump.displayOrder")
    List<UserMenuPermissionEntity> findViewableMenusByUser(@Param("userId") Long userId, @Param("status") Status status);
    
    void deleteByUserIdAndMenuItemId(Long userId, Long menuItemId);
    
    boolean existsByUserIdAndMenuItemId(Long userId, Long menuItemId);
}