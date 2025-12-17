package com.emenu.features.notification.dto.filter;

import com.emenu.enums.notification.MessageType;
import com.emenu.shared.dto.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class NotificationFilterRequest extends BaseFilterRequest {
    
    private Boolean unreadOnly = false;
    private MessageType messageType;
    private Boolean systemNotificationsOnly = false;
}
