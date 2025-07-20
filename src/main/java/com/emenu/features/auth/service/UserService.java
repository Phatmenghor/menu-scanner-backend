package com.emenu.features.auth.service;

import com.emenu.features.auth.dto.filter.UserFilterRequest;
import com.emenu.features.auth.dto.request.UserCreateRequest;
import com.emenu.features.auth.dto.response.UserResponse;
import com.emenu.features.auth.dto.update.UserUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface UserService {
    
    // CRUD Operations
    UserResponse createUser(UserCreateRequest request);
    PaginationResponse<UserResponse> getUsers(UserFilterRequest filter);
    UserResponse getUserById(UUID id);
    UserResponse updateUser(UUID id, UserUpdateRequest request);
    void deleteUser(UUID id);
    
    // User Status Management
    void activateUser(UUID id);
    void deactivateUser(UUID id);
    void lockUser(UUID id);
    void unlockUser(UUID id);
    
    // Profile Management
    UserResponse getCurrentUserProfile();
    UserResponse updateCurrentUserProfile(UserUpdateRequest request);
}