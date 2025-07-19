package com.emenu.features.user_management.service;

import com.emenu.features.user_management.dto.filter.UserFilterRequest;
import com.emenu.features.user_management.dto.request.PasswordChangeRequest;
import com.emenu.features.user_management.dto.request.UserCreateRequest;
import com.emenu.features.user_management.dto.response.UserResponse;
import com.emenu.features.user_management.dto.response.UserSummaryResponse;
import com.emenu.features.user_management.dto.update.UserUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface UserService {
    
    UserResponse createUser(UserCreateRequest request);
    
    UserResponse getUserById(UUID id);
    
    UserResponse updateUser(UUID id, UserUpdateRequest request);
    
    void deleteUser(UUID id);
    
    PaginationResponse<UserSummaryResponse> getUsers(UserFilterRequest filter);
    
    void changePassword(UUID userId, PasswordChangeRequest request);
    
    void resetPassword(UUID userId, String newPassword);
    
    void toggleUserStatus(UUID userId);
}