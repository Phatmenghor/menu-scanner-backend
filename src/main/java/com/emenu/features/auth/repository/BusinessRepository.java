package com.emenu.features.auth.repository;

import com.emenu.features.auth.models.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessRepository extends JpaRepository<Business, UUID>, JpaSpecificationExecutor<Business> {
    
    // ✅ FIXED: Load business with subscriptions for proper calculation
    @Query("SELECT b FROM Business b " +
           "LEFT JOIN FETCH b.subscriptions s " +
           "LEFT JOIN FETCH s.plan " +
           "WHERE b.id = :id AND b.isDeleted = false")
    Optional<Business> findByIdAndIsDeletedFalse(@Param("id") UUID id);
    
    boolean existsByEmailAndIsDeletedFalse(String email);

    // ✅ ENHANCED: Check business name uniqueness (case-insensitive)
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Business b WHERE LOWER(b.name) = LOWER(:name) AND b.isDeleted = false")
    boolean existsByNameIgnoreCaseAndIsDeletedFalse(@Param("name") String name);
    
    // ✅ NEW: Find business with all relationships for detailed view
    @Query("SELECT b FROM Business b " +
           "LEFT JOIN FETCH b.subscriptions s " +
           "LEFT JOIN FETCH s.plan " +
           "LEFT JOIN FETCH b.users " +
           "WHERE b.id = :id AND b.isDeleted = false")
    Optional<Business> findByIdWithAllRelationships(@Param("id") UUID id);
}