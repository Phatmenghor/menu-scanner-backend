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

    /**
     * Finds a role by name
     */
    Optional<Role> findByName(RoleEnum name);

    /**
     * Finds roles by a list of role names
     */
    List<Role> findByNameIn(List<RoleEnum> names);

    /**
     * Checks if a role exists by name
     */
    boolean existsByName(RoleEnum roleEnum);
}
