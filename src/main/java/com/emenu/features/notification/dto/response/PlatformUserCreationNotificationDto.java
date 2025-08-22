package com.emenu.features.notification.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PlatformUserCreationNotificationDto {
    private String userIdentifier;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String userType;
    private String accountStatus;
    private String roles;
    private String position;
    
    // Creator information
    private String createdByUserIdentifier;
    private String createdByFullName;
    private LocalDateTime createdAt;
}