package com.emenu.features.notification.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BusinessRegistrationNotificationDto {
    private String businessName;
    private String ownerName;
    private String ownerUserIdentifier;
    private String subdomain;
    private LocalDateTime registeredAt;
    private Long ownerTelegramUserId; // For personal notification
}