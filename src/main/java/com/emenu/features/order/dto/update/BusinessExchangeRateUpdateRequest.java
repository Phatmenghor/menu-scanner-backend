package com.emenu.features.order.dto.update;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

@Data
public class BusinessExchangeRateUpdateRequest {
    
    @DecimalMin(value = "1000.0", message = "USD to KHR rate must be at least 1000")
    @DecimalMax(value = "10000.0", message = "USD to KHR rate cannot exceed 10000")
    private Double usdToKhrRate;
    
    // Optional: USD to Chinese Yuan (CNY)
    @DecimalMin(value = "0.1", message = "USD to CNY rate must be at least 0.1")
    @DecimalMax(value = "100.0", message = "USD to CNY rate cannot exceed 100")
    private Double usdToCnyRate;
    
    // Optional: USD to Thai Baht (THB)
    @DecimalMin(value = "1.0", message = "USD to THB rate must be at least 1")
    @DecimalMax(value = "100.0", message = "USD to THB rate cannot exceed 100")
    private Double usdToThbRate;
    
    // Optional: USD to Vietnamese Dong (VND)
    @DecimalMin(value = "1000.0", message = "USD to VND rate must be at least 1000")
    @DecimalMax(value = "50000.0", message = "USD to VND rate cannot exceed 50000")
    private Double usdToVndRate;
    
    private String notes;
}