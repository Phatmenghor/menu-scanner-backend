package com.emenu.features.auth.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

@Data
public class BusinessSettingsRequest {
    
    // Basic Business Information
    private String logoUrl;
    private String name;
    private String description;
    private String phone;
    private String address;
    private String businessType; // Restaurant, Cafe, Bar, Food Truck, Baker, etc.

    // Social Media & Contact
    private String facebookUrl;
    private String instagramUrl;
    private String telegramUrl;

    // Currency Exchange Rate (Frontend calculates prices)
    @DecimalMin(value = "1000.0", message = "Exchange rate must be at least 1000 KHR per USD")
    @DecimalMax(value = "10000.0", message = "Exchange rate cannot exceed 10000 KHR per USD")
    private Double usdToKhrRate;
    
    // Payment & Service Settings for Cambodia
    @DecimalMin(value = "0.0", message = "Tax rate cannot be negative")
    @DecimalMax(value = "100.0", message = "Tax rate cannot exceed 100%")
    private Double taxRate;
}
