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

        Business business = findBusinessById(businessId);
        BusinessSettingsResponse response = businessSettingsMapper.toResponse(business);

        log.debug("Successfully retrieved business settings for: {}", business.getName());
        return response;
    }

    @Override
    @CacheEvict(value = "businessSettings", key = "#businessId")
    public BusinessSettingsResponse updateBusinessSettings(UUID businessId, BusinessSettingsRequest request) {
        log.info("Updating business settings for business ID: {} - Exchange rate: {}",
                businessId, request.getUsdToKhrRate());

        Business business = findBusinessById(businessId);

        // Store original values for audit logging
        BusinessSettingsResponse originalSettings = businessSettingsMapper.toResponse(business);

        // Validate request before updating
        validateBusinessSettingsRequest(request);

        // Use MapStruct to update the entity
        businessSettingsMapper.updateEntity(request, business);

        Business updatedBusiness = businessRepository.save(business);
        BusinessSettingsResponse response = businessSettingsMapper.toResponse(updatedBusiness);

        // Log significant changes
        logSignificantChanges(originalSettings, response);

        log.info("Business settings updated successfully for: {} - Exchange rate: {}",
                business.getName(), business.getUsdToKhrRate());

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

    public boolean validateExchangeRate(Double rate) {
        return businessSettingsMapper.isValidExchangeRate(rate);
    }

    public boolean hasActivePaymentMethod(Business business) {
        return Boolean.TRUE.equals(business.getAcceptsCashPayment()) ||
                Boolean.TRUE.equals(business.getAcceptsOnlinePayment()) ||
                Boolean.TRUE.equals(business.getAcceptsBankTransfer()) ||
                Boolean.TRUE.equals(business.getAcceptsMobilePayment());
    }

    // Private helper methods
    private Business findBusinessById(UUID businessId) {
        return businessRepository.findByIdAndIsDeletedFalse(businessId)
                .orElseThrow(() -> new ValidationException("Business not found with ID: " + businessId));
    }

    private void validateBusinessSettingsRequest(BusinessSettingsRequest request) {
        validateExchangeRateRequest(request);
        validateTaxRateRequest(request);
        validateServiceChargeRateRequest(request);
        validatePaymentMethodsRequest(request);
        validateBusinessInformation(request);
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

    private void validateServiceChargeRateRequest(BusinessSettingsRequest request) {
        if (request.getServiceChargeRate() != null &&
                !businessSettingsMapper.isValidServiceChargeRate(request.getServiceChargeRate())) {
            throw new ValidationException(
                    String.format("Service charge rate %.2f%% is invalid. Must be between 0%% and 100%%",
                            request.getServiceChargeRate()));
        }
    }

    private void validatePaymentMethodsRequest(BusinessSettingsRequest request) {
        if (isAllPaymentMethodsDisabled(request)) {
            throw new ValidationException("At least one payment method must be enabled");
        }
    }

    private void validateBusinessInformation(BusinessSettingsRequest request) {
        // Validate business name length
        if (request.getName() != null && request.getName().trim().length() > 100) {
            throw new ValidationException("Business name cannot exceed 100 characters");
        }

        // Validate description length
        if (request.getDescription() != null && request.getDescription().length() > 1000) {
            throw new ValidationException("Business description cannot exceed 1000 characters");
        }

        // Validate operating hours format (basic validation)
        if (request.getOperatingHours() != null && request.getOperatingHours().length() > 500) {
            throw new ValidationException("Operating hours cannot exceed 500 characters");
        }
    }

    private boolean isAllPaymentMethodsDisabled(BusinessSettingsRequest request) {
        // Only validate if payment method fields are provided in the request
        boolean anyPaymentMethodProvided = request.getAcceptsCashPayment() != null ||
                request.getAcceptsOnlinePayment() != null ||
                request.getAcceptsBankTransfer() != null ||
                request.getAcceptsMobilePayment() != null;

        // If no payment method fields are provided, don't validate
        if (!anyPaymentMethodProvided) {
            return false;
        }

        // Check if all provided payment methods are explicitly disabled
        return Boolean.FALSE.equals(request.getAcceptsCashPayment()) &&
                Boolean.FALSE.equals(request.getAcceptsOnlinePayment()) &&
                Boolean.FALSE.equals(request.getAcceptsBankTransfer()) &&
                Boolean.FALSE.equals(request.getAcceptsMobilePayment());
    }

    private void logSignificantChanges(BusinessSettingsResponse original, BusinessSettingsResponse updated) {
        // Log exchange rate changes
        if (!original.getUsdToKhrRate().equals(updated.getUsdToKhrRate())) {
            log.info("Exchange rate changed from {} to {} for business: {}",
                    original.getUsdToKhrRate(), updated.getUsdToKhrRate(), updated.getName());
        }

        // Log tax rate changes
        if (!original.getTaxRate().equals(updated.getTaxRate())) {
            log.info("Tax rate changed from {}% to {}% for business: {}",
                    original.getTaxRate(), updated.getTaxRate(), updated.getName());
        }

        // Log service charge changes
        if (!original.getServiceChargeRate().equals(updated.getServiceChargeRate())) {
            log.info("Service charge rate changed from {}% to {}% for business: {}",
                    original.getServiceChargeRate(), updated.getServiceChargeRate(), updated.getName());
        }

        // Log payment method changes
        logPaymentMethodChanges(original, updated);
    }

    private void logPaymentMethodChanges(BusinessSettingsResponse original, BusinessSettingsResponse updated) {
        if (!original.getAcceptsCashPayment().equals(updated.getAcceptsCashPayment())) {
            log.info("Cash payment acceptance changed from {} to {} for business: {}",
                    original.getAcceptsCashPayment(), updated.getAcceptsCashPayment(), updated.getName());
        }

        if (!original.getAcceptsOnlinePayment().equals(updated.getAcceptsOnlinePayment())) {
            log.info("Online payment acceptance changed from {} to {} for business: {}",
                    original.getAcceptsOnlinePayment(), updated.getAcceptsOnlinePayment(), updated.getName());
        }

        if (!original.getAcceptsBankTransfer().equals(updated.getAcceptsBankTransfer())) {
            log.info("Bank transfer acceptance changed from {} to {} for business: {}",
                    original.getAcceptsBankTransfer(), updated.getAcceptsBankTransfer(), updated.getName());
        }

        if (!original.getAcceptsMobilePayment().equals(updated.getAcceptsMobilePayment())) {
            log.info("Mobile payment acceptance changed from {} to {} for business: {}",
                    original.getAcceptsMobilePayment(), updated.getAcceptsMobilePayment(), updated.getName());
        }
    }
}