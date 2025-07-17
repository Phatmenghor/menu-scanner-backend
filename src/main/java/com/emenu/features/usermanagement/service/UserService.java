package com.emenu.features.usermanagement.service;

import com.emenu.features.usermanagement.dto.filter.UserFilterRequest;
import com.emenu.features.usermanagement.dto.request.ChangePasswordRequest;
import com.emenu.features.usermanagement.dto.request.CreateUserRequest;
import com.emenu.features.usermanagement.dto.response.UserResponse;
import com.emenu.features.usermanagement.dto.response.UserSummaryResponse;
import com.emenu.features.usermanagement.dto.update.UpdateUserRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface UserService {
    
    UserResponse createUser(CreateUserRequest request);
    
    UserResponse getUserById(UUID id);
    
    UserResponse updateUser(UUID id, UpdateUserRequest request);
    
    void deleteUser(UUID id);
    
    void restoreUser(UUID id);
    
    PaginationResponse<UserSummaryResponse> getUsers(UserFilterRequest filter);
    
    UserResponse changePassword(UUID id, ChangePasswordRequest request);
    
    void lockUser(UUID id);
    
    void unlockUser(UUID id);
    
    void verifyEmail(String token);
    
    void resetPassword(String email);
    
    void resetPasswordWithToken(String token, String newPassword);
    
    UserResponse getCurrentUser();
    
    void addLoyaltyPoints(UUID userId, int points);
    
    void updateCustomerStats(UUID userId, double orderAmount);
}
