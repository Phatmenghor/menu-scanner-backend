package com.emenu.features.auth.service;

import com.emenu.features.auth.dto.filter.UserFilterRequest;
import com.emenu.features.auth.dto.request.UserCreateRequest;
import com.emenu.features.auth.dto.request.UserMessageRequest;
import com.emenu.features.auth.dto.response.UserResponse;
import com.emenu.features.auth.dto.response.UserSummaryResponse;
import com.emenu.features.auth.dto.update.UserUpdateRequest;
import com.emenu.features.messaging.dto.response.MessageResponse;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface UserManagementService {
    
    // User CRUD
    UserResponse createUser(UserCreateRequest request);
    PaginationResponse<UserSummaryResponse> getUsers(UserFilterRequest filter);
    UserResponse getUserById(UUID id);
    UserResponse updateUser(UUID id, UserUpdateRequest request);
    void deleteUser(UUID id);
    void activateUser(UUID id);
    void deactivateUser(UUID id);
    void lockUser(UUID id);
    void unlockUser(UUID id);
    
    // User Messaging
    void sendMessageToUser(UUID userId, UserMessageRequest request);
    PaginationResponse<MessageResponse> getUserMessages(UUID userId, int pageNo, int pageSize);
    void markMessageAsRead(UUID messageId);
    
    // Profile Management
    UserResponse getCurrentUserProfile();
    UserResponse updateCurrentUserProfile(UserUpdateRequest request);
    PaginationResponse<MessageResponse> getCurrentUserMessages(int pageNo, int pageSize);
    PaginationResponse<MessageResponse> getCurrentUserUnreadMessages(int pageNo, int pageSize);
}