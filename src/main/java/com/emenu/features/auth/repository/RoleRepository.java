package com.emenu.features.auth.repository;

import com.emenu.features.auth.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByNameAndIsDeletedFalse(String name);

    List<Role> findByNameInAndIsDeletedFalse(List<String> names);

    boolean existsByNameAndIsDeletedFalse(String name);

    Optional<Role> findByNameAndBusinessIdAndIsDeletedFalse(String name, UUID businessId);

    Optional<Role> findByNameAndBusinessIdIsNullAndIsDeletedFalse(String name);

    List<Role> findByBusinessIdAndIsDeletedFalse(UUID businessId);

    List<Role> findByBusinessIdIsNullAndIsDeletedFalse();

    boolean existsByNameAndBusinessIdAndIsDeletedFalse(String name, UUID businessId);
}
