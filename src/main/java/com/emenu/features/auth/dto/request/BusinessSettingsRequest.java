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
    private String website;
    
    // Business Type for Cambodia
    private String businessType; // Restaurant, Cafe, Bar, Food Truck, Bakery
    private String cuisineType;  // Khmer, Chinese, Thai, Vietnamese, Western, Mixed
    
    // Operating Hours
    private String operatingHours; // "Mon-Sun: 6AM-10PM" or JSON format
    
    // Social Media & Contact
    private String facebookUrl;
    private String instagramUrl;
    private String telegramContact;
    
    // Currency Exchange Rate (Frontend calculates prices)
    @DecimalMin(value = "1000.0", message = "Exchange rate must be at least 1000 KHR per USD")
    @DecimalMax(value = "10000.0", message = "Exchange rate cannot exceed 10000 KHR per USD")
    private Double usdToKhrRate;
    
    // Payment & Service Settings for Cambodia
    @DecimalMin(value = "0.0", message = "Tax rate cannot be negative")
    @DecimalMax(value = "100.0", message = "Tax rate cannot exceed 100%")
    private Double taxRate;
    
    @DecimalMin(value = "0.0", message = "Service charge cannot be negative")
    @DecimalMax(value = "100.0", message = "Service charge cannot exceed 100%")
    private Double serviceChargeRate;
    
    // Payment Methods Available in Cambodia
    private Boolean acceptsOnlinePayment;
    private Boolean acceptsCashPayment;
    private Boolean acceptsBankTransfer;
    private Boolean acceptsMobilePayment; // ABA, Wing, Pi Pay, etc.
}
