package com.emenu.features.notification.dto.resposne;

import com.emenu.enums.notification.MessageStatus;
import com.emenu.enums.notification.MessageType;
import com.emenu.enums.notification.NotificationChannel;
import com.emenu.shared.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class NotificationResponse extends BaseAuditResponse {

    private String title;
    private String message;
    private MessageType messageType;
    private NotificationChannel channel;
    private MessageStatus status;
    
    private UUID userId;
    private String userName;
    
    private UUID businessId;
    
    private Boolean isSystemCopy;
    private Boolean isRead;
    private LocalDateTime readAt;
    
    private String referenceType;
    private UUID referenceId;
    private String actionUrl;
}
