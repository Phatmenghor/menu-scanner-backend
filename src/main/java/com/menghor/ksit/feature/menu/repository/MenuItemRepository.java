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

    /**
     * Find all active menu items ordered by display order
     */
    List<MenuItemEntity> findByStatusOrderByDisplayOrderAscIdAsc(Status status);

    /**
     * Find all menu items (including inactive) ordered by display order
     */
    List<MenuItemEntity> findAllByOrderByDisplayOrderAscIdAsc();

    /**
     * Find parent menu items only
     */
    @Query("SELECT m FROM MenuItemEntity m " +
           "WHERE m.isParent = true " +
           "AND m.status = :status " +
           "ORDER BY m.displayOrder ASC, m.id ASC")
    List<MenuItemEntity> findParentMenusByStatus(@Param("status") Status status);

    /**
     * Find child menu items for a specific parent
     */
    @Query("SELECT m FROM MenuItemEntity m " +
           "WHERE m.parent.id = :parentId " +
           "AND m.status = :status " +
           "ORDER BY m.displayOrder ASC, m.id ASC")
    List<MenuItemEntity> findChildMenusByParentIdAndStatus(@Param("parentId") Long parentId,
                                                          @Param("status") Status status);

    /**
     * Find menu by code
     */
    Optional<MenuItemEntity> findByCodeAndStatus(String code, Status status);

    /**
     * Check if menu code exists
     */
    boolean existsByCodeAndStatus(String code, Status status);

    /**
     * Get max display order
     */
    @Query("SELECT COALESCE(MAX(m.displayOrder), 0) FROM MenuItemEntity m WHERE m.status = :status")
    Integer getMaxDisplayOrder(@Param("status") Status status);

    /**
     * Get max display order for parent menus
     */
    @Query("SELECT COALESCE(MAX(m.displayOrder), 0) FROM MenuItemEntity m " +
           "WHERE m.isParent = true AND m.status = :status")
    Integer getMaxDisplayOrderForParents(@Param("status") Status status);

    /**
     * Get max display order for child menus under a parent
     */
    @Query("SELECT COALESCE(MAX(m.displayOrder), 0) FROM MenuItemEntity m " +
           "WHERE m.parent.id = :parentId AND m.status = :status")
    Integer getMaxDisplayOrderForChildren(@Param("parentId") Long parentId, 
                                         @Param("status") Status status);
}