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
    PaginationResponse<UserResponse> getUsers(UserFilterRequest filter);
    List<UserResponse> getAllUsers();
    UserResponse getUserById(UUID userId);
    UserResponse getUserByEmail(String email);
    UserResponse updateUser(UUID userId, UserUpdateRequest request);
    UserResponse deleteUser(UUID userId);

    // Current User Operations
    UserResponse getCurrentUser();
    UserResponse updateCurrentUser(UserUpdateRequest request);

    // User Validation
    boolean existsByEmail(String email);
    boolean existsByPhone(String phoneNumber);
}