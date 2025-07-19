package com.emenu.features.user_management.service;

import com.emenu.features.user_management.dto.filter.UserFilterRequest;
import com.emenu.features.user_management.dto.request.UserCreateRequest;
import com.emenu.features.user_management.dto.response.UserResponse;
import com.emenu.features.user_management.dto.response.UserSummaryResponse;
import com.emenu.features.user_management.dto.update.UserUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface CustomerService {
    
    UserResponse registerCustomer(UserCreateRequest request);
    
    PaginationResponse<UserSummaryResponse> getCustomers(UserFilterRequest filter);
    
    UserResponse updateCustomer(UUID id, UserUpdateRequest request);
    
    void deleteCustomer(UUID id);
    
    // Customer specific functions
    void addLoyaltyPoints(UUID customerId, Integer points);
    
    void upgradeTier(UUID customerId);
    
    UserResponse getCustomerProfile(UUID customerId);
}
