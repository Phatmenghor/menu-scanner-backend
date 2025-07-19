package com.emenu.features.user_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private long expiresIn;
    
    // User info
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String userType;
    private List<String> roles;
    private boolean emailVerified;
    
    // Business info (if applicable)
    private UUID businessId;
    private String subscriptionPlan;
    private String subscriptionStatus;
    
    private LocalDateTime loginTime;
}
