package com.emenu.features.auth.repository;

import com.emenu.enums.user.UserType;
import com.emenu.features.auth.models.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByNameAndIsDeletedFalse(String name);

    Optional<Role> findByIdAndIsDeletedFalse(UUID id);

    List<Role> findByNameInAndIsDeletedFalse(List<String> names);

    boolean existsByNameAndIsDeletedFalse(String name);

    Optional<Role> findByNameAndBusinessIdAndIsDeletedFalse(String name, UUID businessId);

    Optional<Role> findByNameAndBusinessIdIsNullAndIsDeletedFalse(String name);

    List<Role> findByBusinessIdAndIsDeletedFalse(UUID businessId);

    List<Role> findByBusinessIdIsNullAndIsDeletedFalse();

    List<Role> findByUserTypeAndIsDeletedFalse(UserType userType);

    List<Role> findByUserTypeAndBusinessIdAndIsDeletedFalse(UserType userType, UUID businessId);

    boolean existsByNameAndBusinessIdAndIsDeletedFalse(String name, UUID businessId);

    boolean existsByNameAndBusinessIdIsNullAndIsDeletedFalse(String name);

    /**
     * Find all roles with filtering and pagination
     */
    @Query("SELECT r FROM Role r WHERE r.isDeleted = false " +
            "AND (:businessId IS NULL OR r.businessId = :businessId) " +
            "AND (:platformRolesOnly IS NULL OR :platformRolesOnly = false OR r.businessId IS NULL) " +
            "AND (:userTypes IS NULL OR r.userType IN :userTypes) " +
            "AND (:search IS NULL OR :search = '' OR " +
            "LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(r.displayName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Role> findAllWithFilters(
            @Param("businessId") UUID businessId,
            @Param("platformRolesOnly") Boolean platformRolesOnly,
            @Param("userTypes") List<UserType> userTypes,
            @Param("search") String search,
            Pageable pageable);

    /**
     * Count all roles with filtering
     */
    @Query("SELECT COUNT(r) FROM Role r WHERE r.isDeleted = false " +
            "AND (:businessId IS NULL OR r.businessId = :businessId) " +
            "AND (:platformRolesOnly IS NULL OR :platformRolesOnly = false OR r.businessId IS NULL) " +
            "AND (:userTypes IS NULL OR r.userType IN :userTypes) " +
            "AND (:search IS NULL OR :search = '' OR " +
            "LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(r.displayName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    long countAllWithFilters(
            @Param("businessId") UUID businessId,
            @Param("platformRolesOnly") Boolean platformRolesOnly,
            @Param("userTypes") List<UserType> userTypes,
            @Param("search") String search);
}
