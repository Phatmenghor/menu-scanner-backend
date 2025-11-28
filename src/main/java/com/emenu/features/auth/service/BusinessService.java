package com.emenu.features.auth.service;

import com.emenu.features.auth.dto.filter.BusinessFilterRequest;
import com.emenu.features.auth.dto.request.BusinessCreateRequest;
import com.emenu.features.auth.dto.response.BusinessResponse;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface BusinessService {

    BusinessResponse createBusiness(BusinessCreateRequest request);
    
    PaginationResponse<BusinessResponse> getAllBusinesses(BusinessFilterRequest request);
    
    BusinessResponse getBusinessById(UUID businessId);
    
    BusinessResponse updateBusiness(UUID businessId, BusinessCreateRequest request);
    
    void deleteBusiness(UUID businessId);
}