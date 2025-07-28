package com.emenu.features.auth.repository;

import com.emenu.enums.user.AccountStatus;
import com.emenu.enums.user.UserType;
import com.emenu.features.auth.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    // ✅ NEW: Primary login method using userIdentifier
    Optional<User> findByUserIdentifierAndIsDeletedFalse(String userIdentifier);
    boolean existsByUserIdentifierAndIsDeletedFalse(String userIdentifier);

    // ✅ UPDATED: Keep email methods for backward compatibility (but no uniqueness required)
    Optional<User> findByEmailAndIsDeletedFalse(String email);

    Optional<User> findByIdAndIsDeletedFalse(UUID id);

    @Query("SELECT COUNT(u) FROM User u WHERE u.businessId = :businessId AND u.isDeleted = false")
    long countByBusinessIdAndIsDeletedFalse(@Param("businessId") UUID businessId);
}