package com.emenu.features.notification.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TelegramLinkNotificationDto {
    private String userIdentifier;
    private String fullName;
    private String userType;
    private String telegramUsername;
    private Long telegramUserId;
    private LocalDateTime linkedAt;
}