package com.emenu.features.auth.service;

import com.emenu.features.auth.dto.request.BusinessSettingCreateRequest;
import com.emenu.features.auth.dto.response.BusinessSettingResponse;
import com.emenu.features.auth.dto.update.BusinessSettingUpdateRequest;

import java.util.UUID;

public interface BusinessSettingService {

    BusinessSettingResponse createBusinessSetting(BusinessSettingCreateRequest request);
    
    BusinessSettingResponse getBusinessSettingByBusinessId(UUID businessId);
    
    BusinessSettingResponse updateBusinessSetting(UUID businessId, BusinessSettingUpdateRequest request);
    
    void deleteBusinessSetting(UUID businessId);
    
    BusinessSettingResponse getCurrentBusinessSetting();
}