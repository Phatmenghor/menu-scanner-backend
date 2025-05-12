package com.menghor.ksit.feature.auth.repository;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.feature.auth.models.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {

    @EntityGraph(attributePaths = {"roles", "classes", "department"})
    Optional<UserEntity> findByUsername(String username);

    boolean existsByUsername(String username);

    @EntityGraph(attributePaths = {"roles", "classes", "department"})
    @Query("SELECT u FROM UserEntity u JOIN FETCH u.roles WHERE u.id = :id")
    Optional<UserEntity> findUserWithRolesById(@Param("id") Long id);

    @Query("SELECT u FROM UserEntity u JOIN u.roles r WHERE r.name = :roleName")
    Page<UserEntity> findByRoleName(@Param("roleName") RoleEnum roleName, Pageable pageable);

    @Query("SELECT u FROM UserEntity u JOIN u.roles r WHERE r.name IN :roleNames")
    Page<UserEntity> findByRoleNames(@Param("roleNames") List<RoleEnum> roleNames, Pageable pageable);

    @Query("SELECT u FROM UserEntity u JOIN u.classes c WHERE c.id = :classId")
    List<UserEntity> findStudentsByClassId(@Param("classId") Long classId);

    @Query("SELECT u FROM UserEntity u WHERE u.department.id = :departmentId")
    List<UserEntity> findUsersByDepartmentId(@Param("departmentId") Long departmentId);

    @Query("SELECT COUNT(u) FROM UserEntity u JOIN u.roles r WHERE r.name = :roleName")
    Long countByRole(@Param("roleName") RoleEnum roleName);

    /**
     * Find users by identifyNumber pattern (for generating sequential identifiers)
     * Used for finding the highest sequence number for a given class prefix
     */
    @Query("SELECT u FROM UserEntity u WHERE u.identifyNumber LIKE :pattern")
    List<UserEntity> findByIdentifyNumberLike(@Param("pattern") String pattern);

    boolean existsByUsernameAndIdNot(String username, Long id);

    boolean existsByIdentifyNumberAndIdNot(String identifyNumber, Long id);

    /**
     * Check if identifyNumber exists
     */
    boolean existsByIdentifyNumber(String identifyNumber);
}