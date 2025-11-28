package com.emenu.features.auth.repository;

import com.emenu.features.auth.models.BusinessSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessSettingRepository extends JpaRepository<BusinessSetting, UUID> {

    Optional<BusinessSetting> findByBusinessIdAndIsDeletedFalse(UUID businessId);
    
    Optional<BusinessSetting> findByIdAndIsDeletedFalse(UUID id);
    
    boolean existsByBusinessIdAndIsDeletedFalse(UUID businessId);
}