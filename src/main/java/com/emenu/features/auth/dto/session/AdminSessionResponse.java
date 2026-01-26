package com.emenu.features.auth.dto.session;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminSessionResponse {

    private UUID id;

    // User info
    private UUID userId;
    private String userIdentifier;
    private String userFullName;
    private String userType;

    // Device info
    private String deviceId;
    private String deviceName;
    private String deviceType;
    private String deviceDisplayName;
    private String browser;
    private String operatingSystem;

    // Location info
    private String ipAddress;
    private String country;
    private String city;

    // Session info
    private String status;
    private LocalDateTime loginAt;
    private LocalDateTime lastActiveAt;
    private LocalDateTime expiresAt;
    private LocalDateTime loggedOutAt;
    private String logoutReason;
    private Boolean isCurrentSession;
    private Long sessionDurationMinutes;
    private Long inactiveDurationMinutes;
}
