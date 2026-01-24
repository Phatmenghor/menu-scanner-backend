package com.emenu.features.auth.repository;

import com.emenu.features.auth.models.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    Optional<Permission> findByCodeAndIsDeletedFalse(String code);

    List<Permission> findByIsDeletedFalse();

    List<Permission> findByCategoryAndIsDeletedFalse(String category);

    List<Permission> findByIsSystemAndIsDeletedFalse(Boolean isSystem);

    boolean existsByCodeAndIsDeletedFalse(String code);
}
