package com.emenu.features.user_management.service;

import com.emenu.features.user_management.dto.filter.UserFilterRequest;
import com.emenu.features.user_management.dto.request.UserCreateRequest;
import com.emenu.features.user_management.dto.response.UserResponse;
import com.emenu.features.user_management.dto.response.UserSummaryResponse;
import com.emenu.features.user_management.dto.update.UserUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface BusinessUserService {
    
    UserResponse createBusinessUser(UserCreateRequest request);
    
    PaginationResponse<UserSummaryResponse> getBusinessUsers(UUID businessId, UserFilterRequest filter);
    
    PaginationResponse<UserSummaryResponse> getMyBusinessUsers(UserFilterRequest filter);
    
    UserResponse updateBusinessUser(UUID id, UserUpdateRequest request);
    
    void deleteBusinessUser(UUID id);
    
    // Business specific functions
    UserResponse createStaffMember(UserCreateRequest request);
    
    long getBusinessUsersCount(UUID businessId);
}