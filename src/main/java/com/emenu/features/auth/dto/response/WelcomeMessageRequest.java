package com.emenu.features.auth.dto.response;

import lombok.Data;

@Data
public class WelcomeMessageRequest {
    
    private String customMessage;
    private String messageType = "WELCOME";
}