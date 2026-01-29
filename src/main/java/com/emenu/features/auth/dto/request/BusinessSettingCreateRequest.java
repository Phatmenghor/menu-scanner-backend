package com.emenu.features.auth.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class BusinessSettingCreateRequest {

    @NotNull(message = "Business ID is required")
    private UUID businessId;

    private String openingTime;
    private String closingTime;
    private Boolean isOpen24Hours = false;
    private String workingDays;
    private String timezone = "Asia/Phnom_Penh";
    private String currency = "USD";
    private String language = "en";
    private String contactEmail;
    private String contactPhone;
    private String whatsappNumber;
    private String facebookUrl;
    private String instagramUrl;
    private String websiteUrl;
    private String logoUrl;
    private String bannerUrl;
    private String primaryColor;
    private String secondaryColor;
    private Boolean emailNotificationsEnabled = true;
    private Boolean smsNotificationsEnabled = false;
    private Boolean orderNotificationsEnabled = true;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    private Double taxPercentage;
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    private Double serviceChargePercentage;
    private Double minOrderAmount;
    private Double deliveryRadiusKm;
    private String estimatedDeliveryTime;
    private String termsAndConditions;
    private String privacyPolicy;
    private String refundPolicy;
}
