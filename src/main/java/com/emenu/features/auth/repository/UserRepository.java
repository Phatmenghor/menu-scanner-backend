package com.emenu.features.auth.repository;

import com.emenu.enums.user.AccountStatus;
import com.emenu.enums.user.RoleEnum;
import com.emenu.enums.user.UserType;
import com.emenu.features.auth.models.User;
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
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUserIdentifierAndIsDeletedFalse(String userIdentifier);

    boolean existsByUserIdentifierAndIsDeletedFalse(String userIdentifier);

    Optional<User> findByIdAndIsDeletedFalse(UUID id);

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN u.roles r " +
            "WHERE u.isDeleted = false " +
            "AND (:businessId IS NULL OR u.businessId = :businessId) " +
            "AND (:userTypes IS NULL OR u.userType IN :userTypes) " +
            "AND (:accountStatuses IS NULL OR u.accountStatus IN :accountStatuses) " +
            "AND (:roles IS NULL OR r.name IN :roles) " +
            "AND (:search IS NULL OR :search = '' OR " +
            "    LOWER(u.userIdentifier) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "    LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "    LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "    LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> searchUsers(
            @Param("businessId") UUID businessId,
            @Param("userTypes") List<UserType> userTypes,
            @Param("accountStatuses") List<AccountStatus> accountStatuses,
            @Param("roles") List<RoleEnum> roles,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT COUNT(u) FROM User u WHERE u.businessId = :businessId AND u.isDeleted = false")
    long countByBusinessId(@Param("businessId") UUID businessId);

    // Find all users by business
    @Query("SELECT u FROM User u WHERE u.businessId = :businessId AND u.isDeleted = false")
    List<User> findAllByBusinessIdAndIsDeletedFalse(@Param("businessId") UUID businessId);

    // Find users by role
    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN u.roles r " +
            "WHERE r.name = :role AND u.isDeleted = false")
    List<User> findByRoleAndIsDeletedFalse(@Param("role") RoleEnum role);

    // Find all platform users (all roles except CUSTOMER and BUSINESS roles)
    @Query("SELECT u FROM User u WHERE u.userType = 'PLATFORM_USER' AND u.isDeleted = false")
    List<User> findAllPlatformUsers();

    // Find all active users (for ALL_USERS notifications)
    @Query("SELECT u FROM User u WHERE u.accountStatus = 'ACTIVE' AND u.isDeleted = false")
    List<User> findAllActiveUsers();

    /**
     * Check if user exists by email
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.isDeleted = false")
    boolean existsByEmailAndIsDeletedFalse(@Param("email") String email);
}