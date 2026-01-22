package com.emenu.features.auth.repository;

import com.emenu.features.auth.models.BusinessSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessSettingRepository extends JpaRepository<BusinessSetting, UUID> {

    /**
     * Finds non-deleted business settings by business ID
     */
    Optional<BusinessSetting> findByBusinessIdAndIsDeletedFalse(UUID businessId);

    /**
     * Finds non-deleted business settings by ID
     */
    Optional<BusinessSetting> findByIdAndIsDeletedFalse(UUID id);

    /**
     * Checks if non-deleted business settings exist for a given business ID
     */
    boolean existsByBusinessIdAndIsDeletedFalse(UUID businessId);
}