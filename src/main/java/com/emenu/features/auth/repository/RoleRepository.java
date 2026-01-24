package com.emenu.features.auth.repository;

import com.emenu.enums.user.RoleEnum;
import com.emenu.features.auth.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(RoleEnum name);

    List<Role> findByNameIn(List<RoleEnum> names);

    boolean existsByName(RoleEnum roleEnum);

    Optional<Role> findByCodeAndBusinessIdAndIsDeletedFalse(String code, UUID businessId);

    Optional<Role> findByCodeAndBusinessIdIsNullAndIsDeletedFalse(String code);

    List<Role> findByBusinessIdAndIsDeletedFalse(UUID businessId);

    List<Role> findByBusinessIdIsNullAndIsDeletedFalse();

    List<Role> findByIsSystemAndIsDeletedFalse(Boolean isSystem);

    boolean existsByCodeAndBusinessIdAndIsDeletedFalse(String code, UUID businessId);
}
