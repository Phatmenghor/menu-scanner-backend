package com.emenu.features.payment.dto.update;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

@Data
public class ExchangeRateUpdateRequest {
    
    @DecimalMin(value = "1000.0", message = "Exchange rate must be at least 1000 KHR per USD")
    @DecimalMax(value = "10000.0", message = "Exchange rate cannot exceed 10000 KHR per USD")
    private Double usdToKhrRate;
    
    private String notes;
}