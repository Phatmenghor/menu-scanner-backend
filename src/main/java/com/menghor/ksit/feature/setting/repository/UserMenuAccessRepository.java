package com.menghor.ksit.feature.setting.repository;

import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.setting.models.MenuItem;
import com.menghor.ksit.feature.setting.models.UserMenuAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMenuAccessRepository extends JpaRepository<UserMenuAccess, Long> {

    List<UserMenuAccess> findByUser(UserEntity user);

    // Updated queries to match entity field names
    @Query("SELECT uma.menuItem FROM UserMenuAccess uma WHERE uma.user.id = :userId AND uma.hasAccess = true")
    List<MenuItem> findAccessibleMenuItemsByUserId(@Param("userId") Long userId);

    // Updated query to match entity field names
    @Query("SELECT uma.menuItem FROM UserMenuAccess uma WHERE uma.user.id = :userId AND uma.hasAccess = true " +
            "AND uma.menuItem.parent = true ORDER BY uma.menuItem.displayOrder ASC")
    List<MenuItem> findAccessibleParentMenusByUserId(@Param("userId") Long userId);

    // Updated query to match entity field names
    @Query("SELECT uma.menuItem FROM UserMenuAccess uma WHERE uma.user.id = :userId AND uma.hasAccess = true " +
            "AND uma.menuItem.parentKey = :parentKey ORDER BY uma.menuItem.displayOrder ASC")
    List<MenuItem> findAccessibleChildMenusByUserIdAndParent(@Param("userId") Long userId, @Param("parentKey") String parentKey);

    Optional<UserMenuAccess> findByUserAndMenuItem(UserEntity user, MenuItem menuItem);

    boolean existsByUserAndMenuItem(UserEntity user, MenuItem menuItem);

    void deleteByUserAndMenuItem(UserEntity user, MenuItem menuItem);
}