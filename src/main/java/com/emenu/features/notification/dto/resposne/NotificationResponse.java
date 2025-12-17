package com.emenu.features.notification.dto.resposne;

import com.emenu.enums.notification.MessageStatus;
import com.emenu.enums.notification.MessageType;
import com.emenu.enums.notification.NotificationPriority;
import com.emenu.enums.notification.NotificationRecipientType;
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
    private NotificationPriority priority;
    private MessageStatus status;
    
    private NotificationRecipientType recipientType;
    
    private UUID userId;
    private String userName;
    
    private UUID businessId;
    
    private UUID groupId;
    
    private Boolean isSeen;
    private LocalDateTime seenAt;
    
    private Boolean isRead;
    private LocalDateTime readAt;
}