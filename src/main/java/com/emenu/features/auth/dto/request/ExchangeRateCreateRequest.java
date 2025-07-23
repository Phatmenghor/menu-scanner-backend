package com.emenu.features.auth.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ExchangeRateCreateRequest {
    
    private UUID businessId; // NULL for system default
    
    @NotNull(message = "Exchange rate is required")
    @DecimalMin(value = "1000.0", message = "Exchange rate must be at least 1000 KHR per USD")
    @DecimalMax(value = "10000.0", message = "Exchange rate cannot exceed 10000 KHR per USD")
    private Double usdToKhrRate;
    
    private Boolean isSystemDefault = false;
    private String notes;
}
