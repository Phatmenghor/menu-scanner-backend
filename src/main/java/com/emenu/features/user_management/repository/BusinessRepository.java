package com.emenu.features.user_management.repository;

import com.emenu.enums.BusinessStatus;
import com.emenu.features.user_management.models.Business;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessRepository extends JpaRepository<Business, UUID> {
    
    Optional<Business> findByIdAndIsDeletedFalse(UUID id);
    
    Page<Business> findByIsDeletedFalse(Pageable pageable);
    
    Page<Business> findByStatusAndIsDeletedFalse(BusinessStatus status, Pageable pageable);
    
    boolean existsByEmailAndIsDeletedFalse(String email);
}