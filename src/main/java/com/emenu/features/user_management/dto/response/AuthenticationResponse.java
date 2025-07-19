package com.emenu.features.user_management.dto.response;

import com.emenu.enums.UserType;
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
    private String refreshToken;
    private String tokenType = "Bearer";
    private long expiresIn;
    
    // User information
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private UserType userType;
    private List<String> roles;
    private boolean emailVerified;
    private boolean twoFactorEnabled;
    
    // Business information (if applicable)
    private UUID businessId;
    private String businessName;
    
    // Login information
    private LocalDateTime lastLogin;
    private LocalDateTime currentLogin;
}