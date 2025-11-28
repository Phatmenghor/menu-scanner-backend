package com.emenu.features.auth.repository;

import com.emenu.enums.user.AccountStatus;
import com.emenu.enums.user.UserType;
import com.emenu.features.auth.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUserIdentifierAndIsDeletedFalse(String userIdentifier);
    
    boolean existsByUserIdentifierAndIsDeletedFalse(String userIdentifier);
    
    Optional<User> findByIdAndIsDeletedFalse(UUID id);

    @Query("SELECT u FROM User u WHERE u.isDeleted = false " +
           "AND (:businessId IS NULL OR u.businessId = :businessId) " +
           "AND (:userType IS NULL OR u.userType = :userType) " +
           "AND (:accountStatus IS NULL OR u.accountStatus = :accountStatus) " +
           "AND (:search IS NULL OR LOWER(u.userIdentifier) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "    OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "    OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "    OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> searchUsers(
            @Param("businessId") UUID businessId,
            @Param("userType") UserType userType,
            @Param("accountStatus") AccountStatus accountStatus,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT COUNT(u) FROM User u WHERE u.businessId = :businessId AND u.isDeleted = false")
    long countByBusinessId(@Param("businessId") UUID businessId);
}