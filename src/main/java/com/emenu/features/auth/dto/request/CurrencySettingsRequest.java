package com.emenu.features.auth.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CurrencySettingsRequest {
    
    @NotNull(message = "USD to KHR exchange rate is required")
    @DecimalMin(value = "1000.0", message = "Exchange rate must be at least 1000 KHR per USD")
    @DecimalMax(value = "10000.0", message = "Exchange rate cannot exceed 10000 KHR per USD")
    private Double usdToKhrRate;
    
    private Boolean showDualCurrency = true;
    
    @Pattern(regexp = "^(USD|KHR)$", message = "Primary display currency must be USD or KHR")
    private String primaryDisplayCurrency = "USD";
    
    private Boolean showKhrAsWholeNumbers = true;
}
