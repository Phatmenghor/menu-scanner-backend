package com.emenu.features.auth.repository;

import com.emenu.enums.user.BusinessStatus;
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
    
    boolean existsByEmailAndIsDeletedFalse(String email);
}
