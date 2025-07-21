package com.emenu.features.notification.controller;

import com.emenu.features.notification.dto.filter.MessageThreadFilterRequest;
import com.emenu.features.notification.dto.request.MessageCreateRequest;
import com.emenu.features.notification.dto.request.MessageThreadCreateRequest;
import com.emenu.features.notification.dto.response.MessageResponse;
import com.emenu.features.notification.dto.response.MessageThreadResponse;
import com.emenu.features.notification.dto.response.MessagingStatsResponse;
import com.emenu.features.notification.service.MessagingService;
import com.emenu.shared.dto.ApiResponse;
import com.emenu.shared.dto.PaginationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/messaging")
@RequiredArgsConstructor
@Slf4j
public class MessagingController {

    private final MessagingService messagingService;

    /**
     * Create a new message thread
     */
    @PostMapping("/threads")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'PLATFORM_SUPPORT', 'BUSINESS_OWNER', 'BUSINESS_MANAGER', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<MessageThreadResponse>> createThread(@Valid @RequestBody MessageThreadCreateRequest request) {
        log.info("Creating message thread: {}", request.getSubject());
        MessageThreadResponse thread = messagingService.createThread(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Message thread created successfully", thread));
    }

    /**
     * Get message threads with filtering and pagination
     */
    @GetMapping("/threads")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'PLATFORM_SUPPORT')")
    public ResponseEntity<ApiResponse<PaginationResponse<MessageThreadResponse>>> getThreads(@ModelAttribute MessageThreadFilterRequest filter) {
        log.info("Getting message threads with filter");
        PaginationResponse<MessageThreadResponse> threads = messagingService.getThreads(filter);
        return ResponseEntity.ok(ApiResponse.success("Message threads retrieved successfully", threads));
    }

    /**
     * Get a specific message thread
     */
    @GetMapping("/threads/{threadId}")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'PLATFORM_SUPPORT', 'BUSINESS_OWNER', 'BUSINESS_MANAGER', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<MessageThreadResponse>> getThreadById(@PathVariable UUID threadId) {
        log.info("Getting message thread by ID: {}", threadId);
        MessageThreadResponse thread = messagingService.getThreadById(threadId);
        return ResponseEntity.ok(ApiResponse.success("Message thread retrieved successfully", thread));
    }

    /**
     * Send a message to a thread
     */
    @PostMapping("/messages")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'PLATFORM_SUPPORT', 'BUSINESS_OWNER', 'BUSINESS_MANAGER', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(@Valid @RequestBody MessageCreateRequest request) {
        log.info("Sending message to thread: {}", request.getThreadId());
        MessageResponse message = messagingService.sendMessage(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Message sent successfully", message));
    }

    /**
     * Get messages in a thread
     */
    @GetMapping("/threads/{threadId}/messages")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'PLATFORM_SUPPORT', 'BUSINESS_OWNER', 'BUSINESS_MANAGER', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getThreadMessages(@PathVariable UUID threadId) {
        log.info("Getting messages for thread: {}", threadId);
        List<MessageResponse> messages = messagingService.getThreadMessages(threadId);
        return ResponseEntity.ok(ApiResponse.success("Thread messages retrieved successfully", messages));
    }

    /**
     * Mark a message as read
     */
    @PostMapping("/messages/{messageId}/mark-read")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'PLATFORM_SUPPORT', 'BUSINESS_OWNER', 'BUSINESS_MANAGER', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> markMessageAsRead(@PathVariable UUID messageId) {
        log.info("Marking message as read: {}", messageId);
        messagingService.markMessageAsRead(messageId);
        return ResponseEntity.ok(ApiResponse.success("Message marked as read", null));
    }

    /**
     * Close a message thread
     */
    @PostMapping("/threads/{threadId}/close")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'PLATFORM_SUPPORT', 'BUSINESS_OWNER', 'BUSINESS_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> closeThread(@PathVariable UUID threadId) {
        log.info("Closing message thread: {}", threadId);
        messagingService.closeThread(threadId);
        return ResponseEntity.ok(ApiResponse.success("Message thread closed successfully", null));
    }

    /**
     * Reopen a message thread
     */
    @PostMapping("/threads/{threadId}/reopen")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'PLATFORM_SUPPORT', 'BUSINESS_OWNER', 'BUSINESS_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> reopenThread(@PathVariable UUID threadId) {
        log.info("Reopening message thread: {}", threadId);
        messagingService.reopenThread(threadId);
        return ResponseEntity.ok(ApiResponse.success("Message thread reopened successfully", null));
    }

    /**
     * Get current user's message threads
     */
    @GetMapping("/my-threads")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'PLATFORM_SUPPORT', 'BUSINESS_OWNER', 'BUSINESS_MANAGER', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<List<MessageThreadResponse>>> getMyThreads() {
        log.info("Getting current user's message threads");
        // This would need to determine current user ID from SecurityUtils
        List<MessageThreadResponse> threads = messagingService.getUserThreads(UUID.randomUUID()); // Placeholder
        return ResponseEntity.ok(ApiResponse.success("User threads retrieved successfully", threads));
    }

    /**
     * Get current user's open threads
     */
    @GetMapping("/my-threads/open")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'PLATFORM_SUPPORT', 'BUSINESS_OWNER', 'BUSINESS_MANAGER', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<List<MessageThreadResponse>>> getMyOpenThreads() {
        log.info("Getting current user's open message threads");
        List<MessageThreadResponse> threads = messagingService.getOpenThreadsForUser(UUID.randomUUID()); // Placeholder
        return ResponseEntity.ok(ApiResponse.success("Open user threads retrieved successfully", threads));
    }

    /**
     * Get current user's unread message count
     */
    @GetMapping("/my-unread-count")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'PLATFORM_SUPPORT', 'BUSINESS_OWNER', 'BUSINESS_MANAGER', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<Long>> getMyUnreadCount() {
        log.info("Getting current user's unread message count");
        long count = messagingService.getUnreadMessageCount(UUID.randomUUID()); // Placeholder
        return ResponseEntity.ok(ApiResponse.success("Unread message count retrieved successfully", count));
    }

    /**
     * Get business message threads
     */
    @GetMapping("/business/{businessId}/threads")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN') or @securityUtils.hasBusinessAccess(#businessId)")
    public ResponseEntity<ApiResponse<List<MessageThreadResponse>>> getBusinessThreads(@PathVariable UUID businessId) {
        log.info("Getting message threads for business: {}", businessId);
        List<MessageThreadResponse> threads = messagingService.getBusinessThreads(businessId);
        return ResponseEntity.ok(ApiResponse.success("Business threads retrieved successfully", threads));
    }

    /**
     * Get messaging statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<MessagingStatsResponse>> getMessagingStats() {
        log.info("Getting messaging statistics");
        MessagingStatsResponse stats = messagingService.getMessagingStats();
        return ResponseEntity.ok(ApiResponse.success("Messaging statistics retrieved successfully", stats));
    }

    /**
     * Get business messaging statistics
     */
    @GetMapping("/business/{businessId}/stats")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN') or @securityUtils.hasBusinessAccess(#businessId)")
    public ResponseEntity<ApiResponse<MessagingStatsResponse>> getBusinessMessagingStats(@PathVariable UUID businessId) {
        log.info("Getting messaging statistics for business: {}", businessId);
        MessagingStatsResponse stats = messagingService.getBusinessMessagingStats(businessId);
        return ResponseEntity.ok(ApiResponse.success("Business messaging statistics retrieved successfully", stats));
    }
}