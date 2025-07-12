package com.menghor.ksit.feature.menu.repository;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.menu.models.MenuItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItemEntity, Long> {
    
    Optional<MenuItemEntity> findByCode(String code);
    
    List<MenuItemEntity> findByStatusOrderByDisplayOrder(Status status);
    
    List<MenuItemEntity> findByParentIsNullAndStatusOrderByDisplayOrder(Status status);
    
    List<MenuItemEntity> findByParentIdAndStatusOrderByDisplayOrder(Long parentId, Status status);
    
    @Query("SELECT m FROM MenuItemEntity m WHERE m.status = :status AND m.parent IS NULL ORDER BY m.displayOrder")
    List<MenuItemEntity> findTopLevelMenus(@Param("status") Status status);
    
    boolean existsByCode(String code);
}