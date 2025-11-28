package com.emenu.features.auth.dto.update;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

@Data
public class BusinessSettingUpdateRequest {

    private String openingTime;
    private String closingTime;
    private Boolean isOpen24Hours;
    private String workingDays;

    private String timezone;
    private String currency;
    private String language;

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

    private Boolean emailNotificationsEnabled;
    private Boolean smsNotificationsEnabled;
    private Boolean orderNotificationsEnabled;

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
