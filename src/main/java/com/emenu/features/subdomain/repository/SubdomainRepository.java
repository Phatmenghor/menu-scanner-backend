package com.emenu.features.subdomain.repository;

import com.emenu.enums.subdomain.SubdomainStatus;
import com.emenu.features.subdomain.models.Subdomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubdomainRepository extends JpaRepository<Subdomain, UUID>, JpaSpecificationExecutor<Subdomain> {
    
    // Basic CRUD operations
    Optional<Subdomain> findByIdAndIsDeletedFalse(UUID id);
    
    // Find by subdomain name
    Optional<Subdomain> findBySubdomainAndIsDeletedFalse(String subdomain);
    
    // Find by business
    Optional<Subdomain> findByBusinessIdAndIsDeletedFalse(UUID businessId);

    // Check existence
    boolean existsBySubdomainAndIsDeletedFalse(String subdomain);
    boolean existsByBusinessIdAndIsDeletedFalse(UUID businessId);
    
    // Check subdomain availability for frontend
    @Query("SELECT s FROM Subdomain s " +
           "JOIN FETCH s.business b " +
           "WHERE s.subdomain = :subdomain " +
           "AND s.isDeleted = false " +
           "AND b.isDeleted = false")
    Optional<Subdomain> findBySubdomainWithBusiness(@Param("subdomain") String subdomain);
    
    // Update access count
    @Modifying
    @Query("UPDATE Subdomain s SET s.accessCount = s.accessCount + 1, s.lastAccessed = :accessTime " +
           "WHERE s.id = :id")
    void incrementAccessCount(@Param("id") UUID id, @Param("accessTime") LocalDateTime accessTime);


    // ✅ FIXED: Changed hasActiveSubscription to isSubscriptionActive
    @Query("SELECT s FROM Subdomain s " +
           "JOIN s.business b " +
           "WHERE s.isDeleted = false " +
           "AND b.isDeleted = false " +
           "AND (b.isSubscriptionActive = false OR b.subscriptionEndDate < :now)")
    List<Subdomain> findExpiredSubdomains(@Param("now") LocalDateTime now);
    
    // Statistics queries
    @Query("SELECT COUNT(s) FROM Subdomain s WHERE s.isDeleted = false")
    long countTotalSubdomains();
    
    @Query("SELECT COUNT(s) FROM Subdomain s WHERE s.isActive = true AND s.isDeleted = false")
    long countActiveSubdomains();
    
    @Query("SELECT COUNT(s) FROM Subdomain s WHERE s.status = :status AND s.isDeleted = false")
    long countByStatus(@Param("status") SubdomainStatus status);

}