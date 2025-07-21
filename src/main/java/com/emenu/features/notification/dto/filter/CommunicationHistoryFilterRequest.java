package com.emenu.features.notification.dto.filter;

import com.emenu.enums.notification.NotificationChannel;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CommunicationHistoryFilterRequest {
    
    private UUID recipientId;
    private UUID senderId;
    private UUID businessId;
    private NotificationChannel channel;
    private String status;
    private LocalDateTime sentAfter;
    private LocalDateTime sentBefore;
    private String search;
    
    // Pagination
    private Integer pageNo = 1;
    private Integer pageSize = 10;
    private String sortBy = "sentAt";
    private String sortDirection = "DESC";
}