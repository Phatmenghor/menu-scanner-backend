package com.emenu.features.auth.service;

import com.emenu.features.auth.dto.request.BusinessSettingsRequest;
import com.emenu.features.auth.dto.response.BusinessSettingsResponse;

import java.util.UUID;

public interface BusinessSettingsService {
    
    // Get business settings
    BusinessSettingsResponse getBusinessSettings(UUID businessId);
    
    // Update business settings
    BusinessSettingsResponse updateBusinessSettings(UUID businessId, BusinessSettingsRequest request);
    
    // Update business logo
    String updateLogo(UUID businessId, String logoUrl);
    
    // Get current user's business settings
    BusinessSettingsResponse getCurrentUserBusinessSettings();
    
    // Update current user's business settings
    BusinessSettingsResponse updateCurrentUserBusinessSettings(BusinessSettingsRequest request);
}