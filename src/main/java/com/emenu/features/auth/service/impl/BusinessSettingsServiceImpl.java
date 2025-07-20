package com.emenu.features.auth.service.impl;

import com.emenu.exception.ValidationException;
import com.emenu.features.auth.dto.request.BusinessSettingsRequest;
import com.emenu.features.auth.dto.response.BusinessSettingsResponse;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.auth.service.BusinessSettingsService;
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
public class BusinessSettingsServiceImpl implements BusinessSettingsService {

    private final BusinessRepository businessRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional(readOnly = true)
    public BusinessSettingsResponse getBusinessSettings(UUID businessId) {
        Business business = businessRepository.findByIdAndIsDeletedFalse(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        return mapToSettingsResponse(business);
    }

    @Override
    public BusinessSettingsResponse updateBusinessSettings(UUID businessId, BusinessSettingsRequest request) {
        Business business = businessRepository.findByIdAndIsDeletedFalse(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        // Update basic information
        if (request.getName() != null) business.setName(request.getName());
        if (request.getDescription() != null) business.setDescription(request.getDescription());
        if (request.getPhone() != null) business.setPhone(request.getPhone());
        if (request.getAddress() != null) business.setAddress(request.getAddress());
        if (request.getWebsite() != null) business.setWebsite(request.getWebsite());
        if (request.getLogoUrl() != null) business.setLogoUrl(request.getLogoUrl());
        
        // Update business details
        if (request.getBusinessType() != null) business.setBusinessType(request.getBusinessType());
        if (request.getCuisineType() != null) business.setCuisineType(request.getCuisineType());
        if (request.getOperatingHours() != null) business.setOperatingHours(request.getOperatingHours());
        
        // Update social media
        if (request.getFacebookUrl() != null) business.setFacebookUrl(request.getFacebookUrl());
        if (request.getInstagramUrl() != null) business.setInstagramUrl(request.getInstagramUrl());
        if (request.getTelegramContact() != null) business.setTelegramContact(request.getTelegramContact());
        
        // Update currency exchange rate
        if (request.getUsdToKhrRate() != null) business.setUsdToKhrRate(request.getUsdToKhrRate());
        
        // Update pricing settings
        if (request.getTaxRate() != null) business.setTaxRate(request.getTaxRate());
        if (request.getServiceChargeRate() != null) business.setServiceChargeRate(request.getServiceChargeRate());
        
        // Update payment methods
        if (request.getAcceptsOnlinePayment() != null) business.setAcceptsOnlinePayment(request.getAcceptsOnlinePayment());
        if (request.getAcceptsCashPayment() != null) business.setAcceptsCashPayment(request.getAcceptsCashPayment());
        if (request.getAcceptsBankTransfer() != null) business.setAcceptsBankTransfer(request.getAcceptsBankTransfer());
        if (request.getAcceptsMobilePayment() != null) business.setAcceptsMobilePayment(request.getAcceptsMobilePayment());

        Business updatedBusiness = businessRepository.save(business);
        log.info("Business settings updated for: {} - Exchange rate: {}", business.getName(), business.getUsdToKhrRate());

        return mapToSettingsResponse(updatedBusiness);
    }

    @Override
    public String updateLogo(UUID businessId, String logoUrl) {
        Business business = businessRepository.findByIdAndIsDeletedFalse(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        business.setLogoUrl(logoUrl);
        businessRepository.save(business);

        log.info("Logo updated for business: {}", business.getName());
        return logoUrl;
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessSettingsResponse getCurrentUserBusinessSettings() {
        User currentUser = securityUtils.getCurrentUser();
        
        if (currentUser.getBusinessId() == null) {
            throw new ValidationException("User is not associated with any business");
        }

        return getBusinessSettings(currentUser.getBusinessId());
    }

    @Override
    public BusinessSettingsResponse updateCurrentUserBusinessSettings(BusinessSettingsRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        
        if (currentUser.getBusinessId() == null) {
            throw new ValidationException("User is not associated with any business");
        }

        return updateBusinessSettings(currentUser.getBusinessId(), request);
    }

    // Helper method to map Business entity to Settings Response
    private BusinessSettingsResponse mapToSettingsResponse(Business business) {
        BusinessSettingsResponse response = new BusinessSettingsResponse();
        
        // Basic info
        response.setBusinessId(business.getId());
        response.setName(business.getName());
        response.setLogoUrl(business.getLogoUrl());
        response.setDescription(business.getDescription());
        response.setPhone(business.getPhone());
        response.setAddress(business.getAddress());
        response.setWebsite(business.getWebsite());
        
        // Business details
        response.setBusinessType(business.getBusinessType());
        response.setCuisineType(business.getCuisineType());
        response.setOperatingHours(business.getOperatingHours());
        
        // Social media
        response.setFacebookUrl(business.getFacebookUrl());
        response.setInstagramUrl(business.getInstagramUrl());
        response.setTelegramContact(business.getTelegramContact());
        
        // Currency exchange rate (Frontend will use for calculations)
        response.setUsdToKhrRate(business.getUsdToKhrRate());
        
        // Pricing
        response.setTaxRate(business.getTaxRate());
        response.setServiceChargeRate(business.getServiceChargeRate());
        
        // Payment methods
        response.setAcceptsOnlinePayment(business.getAcceptsOnlinePayment());
        response.setAcceptsCashPayment(business.getAcceptsCashPayment());
        response.setAcceptsBankTransfer(business.getAcceptsBankTransfer());
        response.setAcceptsMobilePayment(business.getAcceptsMobilePayment());
        
        // System settings (Fixed for Cambodia)
        response.setCurrency(business.getCurrency()); // Always "USD"
        response.setTimezone(business.getTimezone()); // Always "Asia/Phnom_Penh"
        
        // Subscription info (read-only)
        response.setHasActiveSubscription(business.hasActiveSubscription());
        response.setDaysRemaining(business.getDaysRemaining());
        response.setSubscriptionEndDate(business.getSubscriptionEndDate());
        
        // Get current plan from active subscription
        if (business.hasActiveSubscription() && business.getSubscriptions() != null) {
            business.getSubscriptions().stream()
                    .filter(sub -> sub.getIsActive() && !sub.isExpired())
                    .findFirst()
                    .ifPresent(subscription -> response.setCurrentPlan(subscription.getPlan().getDisplayName()));
        }
        
        response.setUpdatedAt(business.getUpdatedAt());
        
        return response;
    }
}