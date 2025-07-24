package com.emenu.features.auth.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ExchangeRateResponse {
    private UUID id;
    private Double usdToKhrRate;
    private String formattedRate; // "1 USD = 4000 KHR"
    private Boolean isActive;
    private String notes;
    private String displayName; // Always "System Exchange Rate"
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}