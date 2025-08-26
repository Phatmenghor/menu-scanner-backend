package com.emenu.features.auth.service.impl;

import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.dto.request.BusinessSettingsRequest;
import com.emenu.features.auth.dto.response.BusinessSettingsResponse;
import com.emenu.features.auth.mapper.BusinessSettingsMapper;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.auth.service.BusinessSettingsService;
import com.emenu.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BusinessSettingsServiceImpl implements BusinessSettingsService {

    private final BusinessRepository businessRepository;
    private final SecurityUtils securityUtils;
    private final BusinessSettingsMapper businessSettingsMapper;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "businessSettings", key = "#businessId")
    public BusinessSettingsResponse getBusinessSettings(UUID businessId) {
        log.debug("Getting business settings for business ID: {}", businessId);

        Business business = businessRepository.findByIdAndIsDeletedFalse(businessId)
                .orElseThrow(() -> new ValidationException("Business not found with ID: " + businessId));

        BusinessSettingsResponse response = businessSettingsMapper.toResponse(business);

        log.debug("Successfully retrieved business settings for: {}", business.getName());
        return response;
    }

    @Override
    @CacheEvict(value = "businessSettings", key = "#businessId")
    public BusinessSettingsResponse updateBusinessSettings(UUID businessId, BusinessSettingsRequest request) {
        log.info("Updating business settings for business ID: {} - Exchange rate: {}",
                businessId, request.getUsdToKhrRate());

        Business business = businessRepository.findByIdAndIsDeletedFalse(businessId)
                .orElseThrow(() -> new ValidationException("Business not found with ID: " + businessId));

        // Store original values for audit logging
        Double originalRate = business.getUsdToKhrRate();

        // Validate request before updating
        validateBusinessSettingsRequest(request);

        // Use MapStruct to update the entity
        businessSettingsMapper.updateEntity(request, business);

        Business updatedBusiness = businessRepository.save(business);
        BusinessSettingsResponse response = businessSettingsMapper.toResponse(updatedBusiness);

        log.info("Business settings updated successfully for: {} - Exchange rate: {} -> {}",
                business.getName(), originalRate, updatedBusiness.getUsdToKhrRate());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessSettingsResponse getCurrentUserBusinessSettings() {
        log.debug("Getting settings for current user's business");

        User currentUser = securityUtils.getCurrentUser();

        if (currentUser.getBusinessId() == null) {
            throw new ValidationException("User is not associated with any business");
        }

        return getBusinessSettings(currentUser.getBusinessId());
    }

    @Override
    public BusinessSettingsResponse updateCurrentUserBusinessSettings(BusinessSettingsRequest request) {
        log.info("Updating settings for current user's business - Exchange rate: {}",
                request.getUsdToKhrRate());

        User currentUser = securityUtils.getCurrentUser();

        if (currentUser.getBusinessId() == null) {
            throw new ValidationException("User is not associated with any business");
        }

        return updateBusinessSettings(currentUser.getBusinessId(), request);
    }

    // Enhanced utility methods
    @CacheEvict(value = "businessSettings", key = "#businessId")
    public void clearBusinessSettingsCache(UUID businessId) {
        log.debug("Clearing business settings cache for business ID: {}", businessId);
    }

    private void validateBusinessSettingsRequest(BusinessSettingsRequest request) {
        validateExchangeRateRequest(request);
        validateTaxRateRequest(request);
    }

    private void validateExchangeRateRequest(BusinessSettingsRequest request) {
        if (request.getUsdToKhrRate() != null &&
                !businessSettingsMapper.isValidExchangeRate(request.getUsdToKhrRate())) {
            throw new ValidationException(
                    String.format("Exchange rate %.2f is invalid. Must be between 1000 and 10000 KHR per USD",
                            request.getUsdToKhrRate()));
        }
    }

    private void validateTaxRateRequest(BusinessSettingsRequest request) {
        if (request.getTaxRate() != null &&
                !businessSettingsMapper.isValidTaxRate(request.getTaxRate())) {
            throw new ValidationException(
                    String.format("Tax rate %.2f%% is invalid. Must be between 0%% and 100%%",
                            request.getTaxRate()));
        }
    }

}