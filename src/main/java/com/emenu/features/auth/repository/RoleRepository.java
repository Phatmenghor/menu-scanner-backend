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

    boolean existsByNameAndBusinessIdAndIsDeletedFalse(String name, UUID businessId);

    boolean existsByNameAndBusinessIdIsNullAndIsDeletedFalse(String name);

    /**
     * Find all roles with filtering and pagination
     * Supports includeAll to include soft-deleted items
     */
    @Query("SELECT r FROM Role r WHERE " +
            "(:includeAll = true OR r.isDeleted = false) " +
            "AND (:businessId IS NULL OR r.businessId = :businessId) " +
            "AND (:userTypes IS NULL OR r.userType IN :userTypes) " +
            "AND (:search IS NULL OR :search = '' OR " +
            "LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Role> findAllWithFilters(
            @Param("businessId") UUID businessId,
            @Param("userTypes") List<UserType> userTypes,
            @Param("search") String search,
            @Param("includeAll") Boolean includeAll,
            Pageable pageable);

    /**
     * Find all roles as list with filtering (no pagination)
     * Supports includeAll to include soft-deleted items
     */
    @Query("SELECT r FROM Role r WHERE " +
            "(:includeAll = true OR r.isDeleted = false) " +
            "AND (:businessId IS NULL OR r.businessId = :businessId) " +
            "AND (:userTypes IS NULL OR r.userType IN :userTypes) " +
            "AND (:search IS NULL OR :search = '' OR " +
            "LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Role> findAllListWithFilters(
            @Param("businessId") UUID businessId,
            @Param("userTypes") List<UserType> userTypes,
            @Param("search") String search,
            @Param("includeAll") Boolean includeAll);
}
