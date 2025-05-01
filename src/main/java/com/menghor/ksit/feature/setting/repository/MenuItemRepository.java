package com.menghor.ksit.feature.setting.repository;

import com.menghor.ksit.feature.setting.models.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    Optional<MenuItem> findByMenuKey(String menuKey);

    // Updated method name to match entity field names
    List<MenuItem> findByParentTrueAndEnabledTrueOrderByDisplayOrderAsc();

    // Updated method name to match entity field names
    List<MenuItem> findByParentKeyAndEnabledTrueOrderByDisplayOrderAsc(String parentKey);

    List<MenuItem> findByParentKeyIsNullOrderByDisplayOrderAsc();

    List<MenuItem> findByParentKeyOrderByDisplayOrderAsc(String parentKey);

    boolean existsByMenuKey(String menuKey);
}