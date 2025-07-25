package com.emenu.features.auth.service;

import com.emenu.features.auth.dto.filter.BusinessFilterRequest;
import com.emenu.features.auth.dto.request.BusinessCreateRequest;
import com.emenu.features.auth.dto.response.BusinessResponse;
import com.emenu.features.auth.dto.update.BusinessUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface BusinessService {
    
    // CRUD Operations
    BusinessResponse createBusiness(BusinessCreateRequest request);
    PaginationResponse<BusinessResponse> getBusinesses(BusinessFilterRequest filter);
    BusinessResponse getBusinessById(UUID id);
    BusinessResponse updateBusiness(UUID id, BusinessUpdateRequest request);
    BusinessResponse deleteBusiness(UUID id);
}
