package com.emenu.features.auth.dto.response;

import com.emenu.enums.UserType;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class LoginResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private UUID userId;
    private String email;
    private String fullName;
    private UserType userType;
    private List<String> roles;
    private UUID businessId;
    private String businessName;
    private String welcomeMessage;
}
