package com.emenu.features.user_management.repository;

import com.emenu.enums.RoleEnum;
import com.emenu.features.user_management.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    
    Optional<Role> findByName(RoleEnum name);
    
    boolean existsByName(RoleEnum name);
    
    List<Role> findByNameIn(List<RoleEnum> names);
}
