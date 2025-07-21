package com.emenu.features.auth.service;

import com.emenu.features.auth.dto.filter.UserFilterRequest;
import com.emenu.features.auth.dto.request.UserCreateRequest;
import com.emenu.features.auth.dto.response.UserResponse;
import com.emenu.features.auth.dto.update.UserUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {

    // User Management
    UserResponse createUser(UserCreateRequest request);
    PaginationResponse<UserResponse> getAllUsers(UserFilterRequest request);
    UserResponse getUserById(UUID userId);
    UserResponse updateUser(UUID userId, UserUpdateRequest request);
    UserResponse deleteUser(UUID userId);

    // Current User Operations
    UserResponse getCurrentUser();
    UserResponse updateCurrentUser(UserUpdateRequest request);

}