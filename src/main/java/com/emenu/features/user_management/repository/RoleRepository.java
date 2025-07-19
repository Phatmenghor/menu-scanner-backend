package com.emenu.features.user_management.repository;

import com.emenu.enums.RoleEnum;
import com.emenu.features.user_management.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(RoleEnum name);
    Optional<Role> findByNameAndIsDeletedFalse(RoleEnum name);

    boolean existsByName(RoleEnum name);
    boolean existsByNameAndIsDeletedFalse(RoleEnum name);

    List<Role> findByIsActiveTrue();
    List<Role> findByIsSystemRoleTrue();
    List<Role> findByIsActiveTrueAndIsDeletedFalse();

    @Query("SELECT r FROM Role r WHERE r.name LIKE 'PLATFORM_%' AND r.isDeleted = false")
    List<Role> findPlatformRoles();

    @Query("SELECT r FROM Role r WHERE r.name LIKE 'BUSINESS_%' AND r.isDeleted = false")
    List<Role> findBusinessRoles();

    @Query("SELECT r FROM Role r WHERE r.name LIKE '%CUSTOMER%' AND r.isDeleted = false")
    List<Role> findCustomerRoles();

    @Query("SELECT r FROM Role r WHERE :permission MEMBER OF r.permissions AND r.isDeleted = false")
    List<Role> findByPermission(@Param("permission") String permission);

    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.id = :roleId AND u.isDeleted = false")
    long countUsersByRole(@Param("roleId") UUID roleId);
}
