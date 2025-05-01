package com.menghor.ksit.feature.auth.repository;

import com.menghor.ksit.feature.auth.models.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {

    @EntityGraph(attributePaths = {"roles"})
    Optional<UserEntity> findByUsername(String username);

    boolean existsByUsername(String username);

    @EntityGraph(attributePaths = {"roles"})
    @Query("SELECT u FROM UserEntity u JOIN FETCH u.roles WHERE u.id = :id")
    Optional<UserEntity> findUserWithRolesById(@Param("id") Long id);
}