package com.emenu.features.messaging.controller;

import com.emenu.features.messaging.dto.filter.MessageFilterRequest;
import com.emenu.features.messaging.dto.request.BroadcastMessageRequest;
import com.emenu.features.messaging.dto.request.MessageCreateRequest;
import com.emenu.features.messaging.dto.response.MessageResponse;
import com.emenu.features.messaging.dto.response.MessageStatsResponse;
import com.emenu.features.messaging.dto.response.MessageSummaryResponse;
import com.emenu.features.messaging.dto.update.MessageUpdateRequest;
import com.emenu.features.messaging.service.MessagingService;
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
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Slf4j
public class MessagingController {

    private final MessagingService messagingService;

    // Core Messaging
    @PostMapping
    public ResponseEntity<ApiResponse<MessageResponse>> createMessage(
            @Valid @RequestBody MessageCreateRequest request) {
        log.info("Creating message from current user to: {}", request.getRecipientId());
        MessageResponse message = messagingService.createMessage(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Message sent successfully", message));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<MessageSummaryResponse>>> getMessages(
            @ModelAttribute MessageFilterRequest filter) {
        log.info("Getting messages with filter");
        PaginationResponse<MessageSummaryResponse> messages = messagingService.getMessages(filter);
        return ResponseEntity.ok(ApiResponse.success("Messages retrieved successfully", messages));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MessageResponse>> getMessageById(@PathVariable UUID id) {
        log.info("Getting message by ID: {}", id);
        MessageResponse message = messagingService.getMessageById(id);
        return ResponseEntity.ok(ApiResponse.success("Message retrieved successfully", message));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MessageResponse>> updateMessage(
            @PathVariable UUID id,
            @Valid @RequestBody MessageUpdateRequest request) {
        log.info("Updating message: {}", id);
        MessageResponse message = messagingService.updateMessage(id, request);
        return ResponseEntity.ok(ApiResponse.success("Message updated successfully", message));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(@PathVariable UUID id) {
        log.info("Deleting message: {}", id);
        messagingService.deleteMessage(id);
        return ResponseEntity.ok(ApiResponse.success("Message deleted successfully", null));
    }

    // Message Actions
    @PostMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable UUID id) {
        log.info("Marking message as read: {}", id);
        messagingService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Message marked as read", null));
    }

    @PostMapping("/{id}/unread")
    public ResponseEntity<ApiResponse<Void>> markAsUnread(@PathVariable UUID id) {
        log.info("Marking message as unread: {}", id);
        messagingService.markAsUnread(id);
        return ResponseEntity.ok(ApiResponse.success("Message marked as unread", null));
    }

    // Broadcast Messaging
    @PostMapping("/broadcast")
    public ResponseEntity<ApiResponse<Void>> broadcastMessage(
            @Valid @RequestBody BroadcastMessageRequest request) {
        log.info("Broadcasting message");
        messagingService.broadcastMessage(request);
        return ResponseEntity.ok(ApiResponse.success("Broadcast message sent successfully", null));
    }

    // Inbox Management
    @GetMapping("/inbox")
    public ResponseEntity<ApiResponse<PaginationResponse<MessageSummaryResponse>>> getInbox(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String search) {
        log.info("Getting inbox messages");
        PaginationResponse<MessageSummaryResponse> messages = messagingService.getInbox(pageNo, pageSize, search);
        return ResponseEntity.ok(ApiResponse.success("Inbox messages retrieved successfully", messages));
    }

    @GetMapping("/sent")
    public ResponseEntity<ApiResponse<PaginationResponse<MessageSummaryResponse>>> getSentMessages(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String search) {
        log.info("Getting sent messages");
        PaginationResponse<MessageSummaryResponse> messages = messagingService.getSentMessages(pageNo, pageSize, search);
        return ResponseEntity.ok(ApiResponse.success("Sent messages retrieved successfully", messages));
    }

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<PaginationResponse<MessageSummaryResponse>>> getUnreadMessages(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("Getting unread messages");
        PaginationResponse<MessageSummaryResponse> messages = messagingService.getUnreadMessages(pageNo, pageSize);
        return ResponseEntity.ok(ApiResponse.success("Unread messages retrieved successfully", messages));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<Long>> getUnreadMessageCount() {
        log.info("Getting unread message count");
        Long count = messagingService.getUnreadMessageCount();
        return ResponseEntity.ok(ApiResponse.success("Unread message count retrieved", count));
    }

    // Message Statistics
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<MessageStatsResponse>> getMessageStats() {
        log.info("Getting message statistics");
        MessageStatsResponse stats = messagingService.getMessageStats();
        return ResponseEntity.ok(ApiResponse.success("Message statistics retrieved successfully", stats));
    }

    @GetMapping("/stats/business/{businessId}")
    public ResponseEntity<ApiResponse<MessageStatsResponse>> getBusinessMessageStats(@PathVariable UUID businessId) {
        log.info("Getting message statistics for business: {}", businessId);
        MessageStatsResponse stats = messagingService.getBusinessMessageStats(businessId);
        return ResponseEntity.ok(ApiResponse.success("Business message statistics retrieved successfully", stats));
    }

    // Bulk Operations
    @PostMapping("/bulk/read")
    public ResponseEntity<ApiResponse<Void>> markMultipleAsRead(@RequestBody java.util.List<UUID> messageIds) {
        log.info("Marking multiple messages as read");
        messagingService.markMultipleAsRead(messageIds);
        return ResponseEntity.ok(ApiResponse.success("Messages marked as read", null));
    }

    @PostMapping("/bulk/delete")
    public ResponseEntity<ApiResponse<Void>> deleteMultipleMessages(@RequestBody java.util.List<UUID> messageIds) {
        log.info("Deleting multiple messages");
        messagingService.deleteMultipleMessages(messageIds);
        return ResponseEntity.ok(ApiResponse.success("Messages deleted successfully", null));
    }

    @PostMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        log.info("Marking all messages as read for current user");
        messagingService.markAllAsRead();
        return ResponseEntity.ok(ApiResponse.success("All messages marked as read", null));
    }

    // Message Search
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PaginationResponse<MessageSummaryResponse>>> searchMessages(
            @RequestParam String query,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("Searching messages with query: {}", query);
        PaginationResponse<MessageSummaryResponse> messages = messagingService.searchMessages(query, pageNo, pageSize);
        return ResponseEntity.ok(ApiResponse.success("Search results retrieved successfully", messages));
    }

    // Message by Type
    @GetMapping("/type/{messageType}")
    public ResponseEntity<ApiResponse<PaginationResponse<MessageSummaryResponse>>> getMessagesByType(
            @PathVariable String messageType,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("Getting messages by type: {}", messageType);
        PaginationResponse<MessageSummaryResponse> messages = messagingService.getMessagesByType(messageType, pageNo, pageSize);
        return ResponseEntity.ok(ApiResponse.success("Messages by type retrieved successfully", messages));
    }

    // Message by Priority
    @GetMapping("/priority/{priority}")
    public ResponseEntity<ApiResponse<PaginationResponse<MessageSummaryResponse>>> getMessagesByPriority(
            @PathVariable String priority,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("Getting messages by priority: {}", priority);
        PaginationResponse<MessageSummaryResponse> messages = messagingService.getMessagesByPriority(priority, pageNo, pageSize);
        return ResponseEntity.ok(ApiResponse.success("Messages by priority retrieved successfully", messages));
    }

    // Reply and Forward
    @PostMapping("/{id}/reply")
    public ResponseEntity<ApiResponse<MessageResponse>> replyToMessage(
            @PathVariable UUID id,
            @Valid @RequestBody MessageCreateRequest request) {
        log.info("Replying to message: {}", id);
        MessageResponse message = messagingService.replyToMessage(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Reply sent successfully", message));
    }

    @PostMapping("/{id}/forward")
    public ResponseEntity<ApiResponse<MessageResponse>> forwardMessage(
            @PathVariable UUID id,
            @RequestParam UUID recipientId,
            @RequestParam(required = false) String additionalNote) {
        log.info("Forwarding message: {} to: {}", id, recipientId);
        MessageResponse message = messagingService.forwardMessage(id, recipientId, additionalNote);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Message forwarded successfully", message));
    }
}