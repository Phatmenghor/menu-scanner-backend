package com.emenu.features.auth.dto.helper;

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
public class UserSessionCreateHelper {

    private UUID userId;
    private UUID refreshTokenId;
    private String deviceId;
    private String deviceName;
    private String deviceType;
    private String userAgent;
    private String browser;
    private String operatingSystem;
    private String ipAddress;
    private String location;
    private String status;
    private LocalDateTime loginAt;
    private LocalDateTime lastActiveAt;
    private LocalDateTime expiresAt;
    private Boolean isCurrentSession;
}
