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
public class UserSessionResponse {

    private UUID id;
    private String deviceId;
    private String deviceName;
    private String deviceType;
    private String deviceDisplayName;
    private String browser;
    private String operatingSystem;
    private String ipAddress;
    private String country;
    private String city;
    private String status;
    private LocalDateTime loginAt;
    private LocalDateTime lastActiveAt;
    private LocalDateTime expiresAt;
    private Boolean isCurrentSession;
    private Long sessionDurationMinutes;
    private Long inactiveDurationMinutes;
}
