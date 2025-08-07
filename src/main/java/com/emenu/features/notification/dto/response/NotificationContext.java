package com.emenu.features.notification.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class NotificationContext {
    private String type;
    private String title;
    private Map<String, Object> data;
    private String recipient;
    private LocalDateTime timestamp;
}