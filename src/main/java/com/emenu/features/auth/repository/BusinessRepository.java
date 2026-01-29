package com.emenu.features.auth.repository;

import com.emenu.enums.user.BusinessStatus;
import com.emenu.features.auth.models.Business;
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
public interface BusinessRepository extends JpaRepository<Business, UUID> {

    /**
     * Finds a non-deleted business by ID
     */
    Optional<Business> findByIdAndIsDeletedFalse(UUID id);

    /**
     * Checks if a non-deleted business exists with the given name
     */
    boolean existsByNameAndIsDeletedFalse(String name);

    /**
     * Finds a non-deleted business by owner ID
     */
    @Query("SELECT b FROM Business b WHERE b.ownerId = :ownerId AND b.isDeleted = false")
    Optional<Business> findByOwnerIdAndIsDeletedFalse(@Param("ownerId") UUID ownerId);

    /**
     * Searches businesses with filters for status, subscription status, and text search across multiple fields
     */
    @Query("SELECT b FROM Business b " +
           "WHERE b.isDeleted = false " +
            "AND (:status IS NULL OR b.status IN :status) " +
            "AND (:hasActiveSubscription IS NULL OR b.isSubscriptionActive = :hasActiveSubscription) " +
           "AND (:search IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "    OR LOWER(b.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "    OR LOWER(b.phone) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "    OR LOWER(b.address) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Business> searchBusinesses(
            @Param("status") List<BusinessStatus> status,
            @Param("hasActiveSubscription") Boolean hasActiveSubscription,
            @Param("search") String search,
            Pageable pageable
    );

    /**
     * Check if business exists by email
     */
    @Query("SELECT COUNT(b) > 0 FROM Business b WHERE b.email = :email AND b.isDeleted = false")
    boolean existsByEmailAndIsDeletedFalse(@Param("email") String email);

    /**
     * Find business by email
     */
    @Query("SELECT b FROM Business b WHERE b.email = :email AND b.isDeleted = false")
    Optional<Business> findByEmailAndIsDeletedFalse(@Param("email") String email);

}