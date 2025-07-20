package com.emenu.features.auth.service;

import com.emenu.features.auth.dto.filter.BusinessFilterRequest;
import com.emenu.features.auth.dto.filter.UserFilterRequest;
import com.emenu.features.auth.dto.request.BusinessCreateRequest;
import com.emenu.features.auth.dto.response.BusinessResponse;
import com.emenu.features.auth.dto.response.DashboardStatsResponse;
import com.emenu.features.auth.dto.response.UserResponse;
import com.emenu.features.auth.dto.update.BusinessUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface BusinessService {
    
    // CRUD Operations
    BusinessResponse createBusiness(BusinessCreateRequest request);
    PaginationResponse<BusinessResponse> getBusinesses(BusinessFilterRequest filter);
    BusinessResponse getBusinessById(UUID id);
    BusinessResponse updateBusiness(UUID id, BusinessUpdateRequest request);
    void deleteBusiness(UUID id);
    
    // Business Status Management
    void activateBusiness(UUID id);
    void suspendBusiness(UUID id);
    
    // Business Analytics
    DashboardStatsResponse getBusinessStats(UUID businessId);
    
    // Staff Management
    PaginationResponse<UserResponse> getBusinessStaff(UUID businessId, UserFilterRequest filter);
    boolean canAddMoreStaff(UUID businessId);
}
