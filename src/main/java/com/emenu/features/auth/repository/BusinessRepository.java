package com.emenu.features.auth.repository;

import com.emenu.enums.BusinessStatus;
import com.emenu.features.auth.models.Business;
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
public interface BusinessRepository extends JpaRepository<Business, UUID>, JpaSpecificationExecutor<Business> {
    
    Optional<Business> findByIdAndIsDeletedFalse(UUID id);
    
    List<Business> findByIsDeletedFalse();
    
    Page<Business> findByIsDeletedFalse(Pageable pageable);
    
    Page<Business> findByStatusAndIsDeletedFalse(BusinessStatus status, Pageable pageable);
    
    boolean existsByEmailAndIsDeletedFalse(String email);
    
    @Query("SELECT COUNT(b) FROM Business b WHERE b.isDeleted = false")
    long countByIsDeletedFalse();
    
    @Query("SELECT COUNT(b) FROM Business b WHERE b.status = :status AND b.isDeleted = false")
    long countByStatusAndIsDeletedFalse(@Param("status") BusinessStatus status);
    
    @Query("SELECT b FROM Business b WHERE b.isDeleted = false AND " +
           "(LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Business> findBySearchAndIsDeletedFalse(@Param("search") String search, Pageable pageable);
}