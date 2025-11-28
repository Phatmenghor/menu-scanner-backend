package com.emenu.features.auth.models;

import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "business_settings", indexes = {
        @Index(name = "idx_business_setting_deleted", columnList = "is_deleted"),
        @Index(name = "idx_business_setting_business", columnList = "business_id, is_deleted")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class BusinessSetting extends BaseUUIDEntity {

    @Column(name = "business_id", nullable = false, unique = true)
    private UUID businessId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", insertable = false, updatable = false)
    private Business business;

    // Business Display
    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "banner_url")
    private String bannerUrl;

    @Column(name = "business_type")
    private String businessType;

    // Business Hours
    @Column(name = "opening_time")
    private String openingTime;

    @Column(name = "closing_time")
    private String closingTime;

    @Column(name = "is_open_24_hours")
    private Boolean isOpen24Hours = false;

    @Column(name = "working_days")
    private String workingDays;

    // Localization
    @Column(name = "timezone")
    private String timezone = "Asia/Phnom_Penh";

    @Column(name = "currency")
    private String currency = "USD";

    @Column(name = "language")
    private String language = "en";

    // Currency Exchange
    @Column(name = "usd_to_khr_rate")
    private Double usdToKhrRate = 4000.0;

    // Contact & Social
    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "whatsapp_number")
    private String whatsappNumber;

    @Column(name = "facebook_url")
    private String facebookUrl;

    @Column(name = "instagram_url")
    private String instagramUrl;

    @Column(name = "telegram_url")
    private String telegramUrl;

    @Column(name = "website_url")
    private String websiteUrl;

    // Display Settings
    @Column(name = "primary_color")
    private String primaryColor;

    @Column(name = "secondary_color")
    private String secondaryColor;

    // Notification Settings
    @Column(name = "email_notifications_enabled")
    private Boolean emailNotificationsEnabled = true;

    @Column(name = "sms_notifications_enabled")
    private Boolean smsNotificationsEnabled = false;

    @Column(name = "order_notifications_enabled")
    private Boolean orderNotificationsEnabled = true;

    // Business Policies
    @Column(name = "tax_rate")
    private Double taxRate = 0.0;

    @Column(name = "service_charge_percentage")
    private Double serviceChargePercentage;

    @Column(name = "min_order_amount")
    private Double minOrderAmount;

    @Column(name = "delivery_radius_km")
    private Double deliveryRadiusKm;

    @Column(name = "estimated_delivery_time")
    private String estimatedDeliveryTime;

    // Terms & Policies
    @Column(name = "terms_and_conditions", columnDefinition = "TEXT")
    private String termsAndConditions;

    @Column(name = "privacy_policy", columnDefinition = "TEXT")
    private String privacyPolicy;

    @Column(name = "refund_policy", columnDefinition = "TEXT")
    private String refundPolicy;
}
