package com.emenu.features.auth.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ExchangeRateResponse {
    private UUID id;
    private UUID businessId;
    private String businessName;
    private Double usdToKhrRate;
    private String formattedRate; // "1 USD = 4000 KHR"
    private Boolean isSystemDefault;
    private Boolean isActive;
    private String notes;
    private String displayName; // "System Default Rate" or "Business Name Rate"
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}