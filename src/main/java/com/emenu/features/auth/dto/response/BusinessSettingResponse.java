package com.emenu.features.auth.dto.response;

import com.emenu.shared.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class BusinessSettingResponse extends BaseAuditResponse {

    private UUID businessId;
    private String businessName;
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
    private Double taxPercentage;
    private Double serviceChargePercentage;
    private Double minOrderAmount;
    private Double deliveryRadiusKm;
    private String estimatedDeliveryTime;
    private String termsAndConditions;
    private String privacyPolicy;
    private String refundPolicy;
}