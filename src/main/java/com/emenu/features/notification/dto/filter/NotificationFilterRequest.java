package com.emenu.features.notification.dto.filter;

import com.emenu.enums.notification.AlertType;
import com.emenu.enums.notification.NotificationChannel;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class NotificationFilterRequest {
    
    private UUID recipientId;
    private UUID senderId;
    private UUID businessId;
    private AlertType alertType;
    private NotificationChannel channel;
    private Boolean isRead;
    private Boolean isSent;
    private String deliveryStatus;
    private LocalDateTime sentAfter;
    private LocalDateTime sentBefore;
    private LocalDateTime scheduledAfter;
    private LocalDateTime scheduledBefore;
    private String search;
    
    // Pagination
    private Integer pageNo = 1;
    private Integer pageSize = 10;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}