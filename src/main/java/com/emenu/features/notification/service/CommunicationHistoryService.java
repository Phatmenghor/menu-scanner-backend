package com.emenu.features.notification.service;

import com.emenu.enums.notification.NotificationChannel;
import com.emenu.features.notification.dto.filter.CommunicationHistoryFilterRequest;
import com.emenu.features.notification.dto.response.CommunicationHistoryResponse;
import com.emenu.shared.dto.PaginationResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CommunicationHistoryService {
    
    // History Tracking
    void recordCommunication(UUID recipientId, UUID senderId, NotificationChannel channel,
                             String subject, String content, String status);
    void recordCommunication(UUID recipientId, UUID senderId, UUID businessId, 
                           NotificationChannel channel, String subject, String content, String status);
    
    // History Queries
    PaginationResponse<CommunicationHistoryResponse> getCommunicationHistory(CommunicationHistoryFilterRequest filter);
    List<CommunicationHistoryResponse> getUserCommunicationHistory(UUID userId);
    List<CommunicationHistoryResponse> getBusinessCommunicationHistory(UUID businessId);
    
    // Status Updates
    void markAsDelivered(String externalMessageId);
    void markAsRead(String externalMessageId);
    void markAsFailed(String externalMessageId, String errorMessage);
    
    // Analytics
    long getTotalCommunicationsCount();
    long getCommunicationsByChannel(NotificationChannel channel);
    long getCommunicationsByStatus(String status);
    long getCommunicationsInDateRange(LocalDateTime start, LocalDateTime end);
    
    // Cleanup
    void cleanupOldHistory(int daysToKeep);
    void archiveHistory(LocalDateTime beforeDate);
}