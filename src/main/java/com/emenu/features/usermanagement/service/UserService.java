package com.emenu.features.usermanagement.service;

import com.emenu.enums.SubscriptionPlan;
import com.emenu.features.usermanagement.dto.filter.UserFilterRequest;
import com.emenu.features.usermanagement.dto.request.ChangePasswordRequest;
import com.emenu.features.usermanagement.dto.request.CreateUserRequest;
import com.emenu.features.usermanagement.dto.response.UserResponse;
import com.emenu.features.usermanagement.dto.response.UserSummaryResponse;
import com.emenu.features.usermanagement.dto.update.UpdateUserRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public interface UserService {

    // Core CRUD operations
    UserResponse createUser(CreateUserRequest request);
    UserResponse getUserById(UUID id);
    UserResponse updateUser(UUID id, UpdateUserRequest request);
    void deleteUser(UUID id);
    void restoreUser(UUID id);

    // Listing and filtering
    PaginationResponse<UserSummaryResponse> getUsers(UserFilterRequest filter);

    // Authentication related
    UserResponse changePassword(UUID id, ChangePasswordRequest request);
    void verifyEmail(String token);
    void verifyPhone(String token);
    void resetPassword(String email);
    void resetPasswordWithToken(String token, String newPassword);

    // Account management
    void lockUser(UUID id);
    void unlockUser(UUID id);
    UserResponse getCurrentUser();

    // Customer management
    void addLoyaltyPoints(UUID userId, int points);
    void updateCustomerStats(UUID userId, double orderAmount);

    // Business access management
    void grantBusinessAccess(UUID userId, UUID businessId);
    void revokeBusinessAccess(UUID userId, UUID businessId);

    // Subscription management
    void updateSubscription(UUID userId, SubscriptionPlan plan, LocalDateTime endDate);
}