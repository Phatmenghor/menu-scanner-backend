package com.emenu.features.auth.service.impl;

import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.dto.request.BusinessSettingCreateRequest;
import com.emenu.features.auth.dto.response.BusinessSettingResponse;
import com.emenu.features.auth.dto.update.BusinessSettingUpdateRequest;
import com.emenu.features.auth.mapper.BusinessSettingMapper;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.models.BusinessSetting;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.auth.repository.BusinessSettingRepository;
import com.emenu.features.auth.service.BusinessSettingService;
import com.emenu.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BusinessSettingServiceImpl implements BusinessSettingService {

    private final BusinessSettingRepository businessSettingRepository;
    private final BusinessRepository businessRepository;
    private final BusinessSettingMapper businessSettingMapper;
    private final SecurityUtils securityUtils;

    @Override
    public BusinessSettingResponse createBusinessSetting(BusinessSettingCreateRequest request) {
        log.info("Creating business setting for business: {}", request.getBusinessId());

        Business business = businessRepository.findByIdAndIsDeletedFalse(request.getBusinessId())
                .orElseThrow(() -> new ValidationException("Business not found"));

        if (businessSettingRepository.existsByBusinessIdAndIsDeletedFalse(request.getBusinessId())) {
            throw new ValidationException("Business setting already exists for this business");
        }

        BusinessSetting businessSetting = businessSettingMapper.toEntity(request);
        BusinessSetting savedSetting = businessSettingRepository.save(businessSetting);

        log.info("Business setting created for: {}", business.getName());
        return businessSettingMapper.toResponse(savedSetting);
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessSettingResponse getBusinessSettingByBusinessId(UUID businessId) {
        BusinessSetting businessSetting = businessSettingRepository.findByBusinessIdAndIsDeletedFalse(businessId)
                .orElseThrow(() -> new RuntimeException("Business setting not found"));
        return businessSettingMapper.toResponse(businessSetting);
    }

    @Override
    public BusinessSettingResponse updateBusinessSetting(UUID businessId, BusinessSettingUpdateRequest request) {
        log.info("Updating business setting for: {}", businessId);

        BusinessSetting businessSetting = businessSettingRepository.findByBusinessIdAndIsDeletedFalse(businessId)
                .orElseThrow(() -> new RuntimeException("Business setting not found"));

        businessSettingMapper.updateEntity(request, businessSetting);
        BusinessSetting updatedSetting = businessSettingRepository.save(businessSetting);

        log.info("Business setting updated for: {}", businessId);
        return businessSettingMapper.toResponse(updatedSetting);
    }

    @Override
    public void deleteBusinessSetting(UUID businessId) {
        BusinessSetting businessSetting = businessSettingRepository.findByBusinessIdAndIsDeletedFalse(businessId)
                .orElseThrow(() -> new RuntimeException("Business setting not found"));

        businessSetting.softDelete();
        businessSettingRepository.save(businessSetting);
        log.info("Business setting deleted for: {}", businessId);
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessSettingResponse getCurrentBusinessSetting() {
        User currentUser = securityUtils.getCurrentUser();
        
        if (currentUser.getBusinessId() == null) {
            throw new ValidationException("User is not associated with any business");
        }

        return getBusinessSettingByBusinessId(currentUser.getBusinessId());
    }
}