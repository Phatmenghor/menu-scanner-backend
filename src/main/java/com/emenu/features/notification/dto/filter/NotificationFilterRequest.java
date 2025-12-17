package com.emenu.features.notification.dto.filter;

import com.emenu.enums.notification.MessageType;
import com.emenu.enums.notification.NotificationPriority;
import com.emenu.enums.notification.NotificationRecipientType;
import com.emenu.shared.dto.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class NotificationFilterRequest extends BaseFilterRequest {
    
    private MessageType messageType;
    private NotificationPriority priority;
    private Boolean isRead;
    private NotificationRecipientType recipientType;
    private Boolean systemNotificationsOnly = false;
    private UUID userId;
    private UUID businessId;
}