package com.emenu.features.notification.service;

import com.emenu.features.notification.dto.filter.MessageThreadFilterRequest;
import com.emenu.features.notification.dto.request.MessageCreateRequest;
import com.emenu.features.notification.dto.request.MessageThreadCreateRequest;
import com.emenu.features.notification.dto.response.MessageResponse;
import com.emenu.features.notification.dto.response.MessageThreadResponse;
import com.emenu.features.notification.dto.response.MessagingStatsResponse;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface MessagingService {
    
    // Thread Management
    MessageThreadResponse createThread(MessageThreadCreateRequest request);
    PaginationResponse<MessageThreadResponse> getThreads(MessageThreadFilterRequest filter);
    MessageThreadResponse getThreadById(UUID threadId);
    void closeThread(UUID threadId);
    void reopenThread(UUID threadId);
    
    // Message Management
    MessageResponse sendMessage(MessageCreateRequest request);
    List<MessageResponse> getThreadMessages(UUID threadId);
    MessageResponse getMessageById(UUID messageId);
    void markMessageAsRead(UUID messageId);
    
    // User-specific methods
    List<MessageThreadResponse> getUserThreads(UUID userId);
    List<MessageThreadResponse> getOpenThreadsForUser(UUID userId);
    long getUnreadMessageCount(UUID userId);
    
    // Business-specific methods
    List<MessageThreadResponse> getBusinessThreads(UUID businessId);
    long getBusinessThreadCount(UUID businessId);
    
    // Statistics
    MessagingStatsResponse getMessagingStats();
    MessagingStatsResponse getBusinessMessagingStats(UUID businessId);
}
