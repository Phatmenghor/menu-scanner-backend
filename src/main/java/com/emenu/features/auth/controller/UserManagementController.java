package com.emenu.features.auth.controller;

import com.emenu.features.auth.dto.filter.UserFilterRequest;
import com.emenu.features.auth.dto.request.UserCreateRequest;
import com.emenu.features.auth.dto.request.UserMessageRequest;
import com.emenu.features.auth.dto.response.UserResponse;
import com.emenu.features.auth.dto.response.UserSummaryResponse;
import com.emenu.features.auth.dto.update.UserUpdateRequest;
import com.emenu.features.auth.service.UserManagementService;
import com.emenu.features.messaging.dto.response.MessageResponse;
import com.emenu.shared.dto.ApiResponse;
import com.emenu.shared.dto.PaginationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserManagementController {

    private final UserManagementService userManagementService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserCreateRequest request) {
        log.info("Creating user: {}", request.getEmail());
        UserResponse user = userManagementService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", user));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<UserSummaryResponse>>> getUsers(
            @ModelAttribute UserFilterRequest filter) {
        log.info("Getting users with filter");
        PaginationResponse<UserSummaryResponse> users = userManagementService.getUsers(filter);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        log.info("Getting user by ID: {}", id);
        UserResponse user = userManagementService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("Updating user: {}", id);
        UserResponse user = userManagementService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        log.info("Deleting user: {}", id);
        userManagementService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable UUID id) {
        log.info("Activating user: {}", id);
        userManagementService.activateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User activated successfully", null));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable UUID id) {
        log.info("Deactivating user: {}", id);
        userManagementService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deactivated successfully", null));
    }

    @PostMapping("/{id}/lock")
    public ResponseEntity<ApiResponse<Void>> lockUser(@PathVariable UUID id) {
        log.info("Locking user: {}", id);
        userManagementService.lockUser(id);
        return ResponseEntity.ok(ApiResponse.success("User locked successfully", null));
    }

    @PostMapping("/{id}/unlock")
    public ResponseEntity<ApiResponse<Void>> unlockUser(@PathVariable UUID id) {
        log.info("Unlocking user: {}", id);
        userManagementService.unlockUser(id);
        return ResponseEntity.ok(ApiResponse.success("User unlocked successfully", null));
    }

    // User Messaging
    @PostMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<Void>> sendMessageToUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserMessageRequest request) {
        log.info("Sending message to user: {}", id);
        userManagementService.sendMessageToUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("Message sent successfully", null));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<PaginationResponse<MessageResponse>>> getUserMessages(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("Getting messages for user: {}", id);
        PaginationResponse<MessageResponse> messages = userManagementService.getUserMessages(id, pageNo, pageSize);
        return ResponseEntity.ok(ApiResponse.success("User messages retrieved successfully", messages));
    }

    @PostMapping("/messages/{messageId}/read")
    public ResponseEntity<ApiResponse<Void>> markMessageAsRead(@PathVariable UUID messageId) {
        log.info("Marking message as read: {}", messageId);
        userManagementService.markMessageAsRead(messageId);
        return ResponseEntity.ok(ApiResponse.success("Message marked as read", null));
    }

    // Profile Management
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile() {
        log.info("Getting my profile");
        UserResponse user = userManagementService.getCurrentUserProfile();
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", user));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("Updating my profile");
        UserResponse user = userManagementService.updateCurrentUserProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", user));
    }

    @GetMapping("/me/messages")
    public ResponseEntity<ApiResponse<PaginationResponse<MessageResponse>>> getMyMessages(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("Getting my messages");
        PaginationResponse<MessageResponse> messages = userManagementService.getCurrentUserMessages(pageNo, pageSize);
        return ResponseEntity.ok(ApiResponse.success("Messages retrieved successfully", messages));
    }

    @GetMapping("/me/messages/unread")
    public ResponseEntity<ApiResponse<PaginationResponse<MessageResponse>>> getMyUnreadMessages(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("Getting my unread messages");
        PaginationResponse<MessageResponse> messages = userManagementService.getCurrentUserUnreadMessages(pageNo, pageSize);
        return ResponseEntity.ok(ApiResponse.success("Unread messages retrieved successfully", messages));
    }
}
