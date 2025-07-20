package com.emenu.features.messaging.service;

import com.emenu.features.messaging.dto.filter.MessageFilterRequest;
import com.emenu.features.messaging.dto.request.BroadcastMessageRequest;
import com.emenu.features.messaging.dto.request.MessageCreateRequest;
import com.emenu.features.messaging.dto.response.MessageResponse;
import com.emenu.features.messaging.dto.response.MessageStatsResponse;
import com.emenu.features.messaging.dto.response.MessageSummaryResponse;
import com.emenu.features.messaging.dto.update.MessageUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface MessagingService {
    
    // Core Messaging
    MessageResponse createMessage(MessageCreateRequest request);
    PaginationResponse<MessageSummaryResponse> getMessages(MessageFilterRequest filter);
    MessageResponse getMessageById(UUID id);
    MessageResponse updateMessage(UUID id, MessageUpdateRequest request);
    void deleteMessage(UUID id);
    
    // Message Actions
    void markAsRead(UUID id);
    void markAsUnread(UUID id);
    
    // Broadcast Messaging
    void broadcastMessage(BroadcastMessageRequest request);
    
    // Inbox Management
    PaginationResponse<MessageSummaryResponse> getInbox(int pageNo, int pageSize, String search);
    PaginationResponse<MessageSummaryResponse> getSentMessages(int pageNo, int pageSize, String search);
    PaginationResponse<MessageSummaryResponse> getUnreadMessages(int pageNo, int pageSize);
    Long getUnreadMessageCount();
    
    // Message Statistics
    MessageStatsResponse getMessageStats();
    MessageStatsResponse getBusinessMessageStats(UUID businessId);
    
    // Bulk Operations
    void markMultipleAsRead(List<UUID> messageIds);
    void deleteMultipleMessages(List<UUID> messageIds);
    void markAllAsRead();
    
    // Message Search
    PaginationResponse<MessageSummaryResponse> searchMessages(String query, int pageNo, int pageSize);
    PaginationResponse<MessageSummaryResponse> getMessagesByType(String messageType, int pageNo, int pageSize);
    PaginationResponse<MessageSummaryResponse> getMessagesByPriority(String priority, int pageNo, int pageSize);
    
    // Reply and Forward
    MessageResponse replyToMessage(UUID messageId, MessageCreateRequest request);
    MessageResponse forwardMessage(UUID messageId, UUID recipientId, String additionalNote);
}