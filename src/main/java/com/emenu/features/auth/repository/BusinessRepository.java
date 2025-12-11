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

    Optional<Business> findByIdAndIsDeletedFalse(UUID id);
    
    Optional<Business> findByNameAndIsDeletedFalse(String name);
    
    boolean existsByNameAndIsDeletedFalse(String name);

    @Query("SELECT b FROM Business b WHERE b.ownerId = :ownerId AND b.isDeleted = false")
    Optional<Business> findByOwnerIdAndIsDeletedFalse(@Param("ownerId") UUID ownerId);

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

    @Query("SELECT COUNT(b) FROM Business b WHERE b.status = :status AND b.isDeleted = false")
    long countByStatus(@Param("status") BusinessStatus status);

    @Query("SELECT COUNT(b) FROM Business b WHERE b.isSubscriptionActive = true AND b.isDeleted = false")
    long countActiveSubscriptions();
}