package com.emenu.features.notification.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CustomerRegistrationNotificationDto {
    private String userIdentifier;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String socialProvider;
    private boolean hasTelegram;
    private LocalDateTime registeredAt;
    private Long telegramUserId; // For personal welcome message
}