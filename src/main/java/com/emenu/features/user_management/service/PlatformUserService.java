package com.emenu.features.user_management.service;

import com.emenu.features.user_management.dto.filter.UserFilterRequest;
import com.emenu.features.user_management.dto.request.UserCreateRequest;
import com.emenu.features.user_management.dto.response.UserResponse;
import com.emenu.features.user_management.dto.response.UserSummaryResponse;
import com.emenu.features.user_management.dto.update.UserUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface PlatformUserService {
    
    UserResponse createPlatformUser(UserCreateRequest request);
    
    PaginationResponse<UserSummaryResponse> getAllPlatformUsers(UserFilterRequest filter);
    
    PaginationResponse<UserSummaryResponse> getAllBusinessUsers(UserFilterRequest filter);
    
    PaginationResponse<UserSummaryResponse> getAllCustomers(UserFilterRequest filter);
    
    UserResponse updatePlatformUser(UUID id, UserUpdateRequest request);
    
    void deletePlatformUser(UUID id);
    
    // Admin functions
    void forcePasswordReset(UUID userId);
    
    void lockUser(UUID userId);
    
    void unlockUser(UUID userId);
    
    // Statistics
    long getTotalUsers();
    
    long getPlatformUsersCount();
    
    long getBusinessUsersCount();
    
    long getCustomersCount();
}