package com.emenu.features.auth.models;

import com.emenu.enums.BusinessStatus;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "businesses")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Business extends BaseUUIDEntity {

    // Basic Information
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address")
    private String address;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Business Settings
    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "website")
    private String website;

    @Column(name = "business_type")
    private String businessType; // Restaurant, Cafe, Bar, etc.

    @Column(name = "cuisine_type")
    private String cuisineType; // Khmer, Chinese, Thai, Western, etc.

    @Column(name = "operating_hours", columnDefinition = "TEXT")
    private String operatingHours; // Store as simple text or JSON

    // Contact & Social Media
    @Column(name = "facebook_url")
    private String facebookUrl;

    @Column(name = "instagram_url")
    private String instagramUrl;

    @Column(name = "telegram_contact")
    private String telegramContact;

    // Currency Exchange Rate (Frontend calculates)
    @Column(name = "usd_to_khr_rate")
    private Double usdToKhrRate = 4000.0; // Default: 1 USD = 4000 KHR

    // Cambodia Settings (Fixed)
    @Column(name = "currency", length = 3)
    private String currency = "USD"; // Standard for Cambodia

    @Column(name = "timezone")
    private String timezone = "Asia/Phnom_Penh";

    // Tax & Service Settings
    @Column(name = "tax_rate")
    private Double taxRate = 0.0; // VAT rate in Cambodia

    @Column(name = "service_charge_rate")
    private Double serviceChargeRate = 0.0;

    @Column(name = "accepts_online_payment")
    private Boolean acceptsOnlinePayment = false;

    @Column(name = "accepts_cash_payment")
    private Boolean acceptsCashPayment = true;

    @Column(name = "accepts_bank_transfer")
    private Boolean acceptsBankTransfer = false;

    @Column(name = "accepts_mobile_payment")
    private Boolean acceptsMobilePayment = false; // ABA, Wing, etc.

    // Subscription Related
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BusinessStatus status = BusinessStatus.PENDING;

    @Column(name = "subscription_start_date")
    private LocalDateTime subscriptionStartDate;

    @Column(name = "subscription_end_date")
    private LocalDateTime subscriptionEndDate;

    @Column(name = "is_subscription_active")
    private Boolean isSubscriptionActive = false;

    // Relationships
    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> users;

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Subscription> subscriptions;

    // Business Logic Methods
    public boolean isActive() {
        return BusinessStatus.ACTIVE.equals(status);
    }

    public boolean hasActiveSubscription() {
        return isSubscriptionActive && 
               subscriptionEndDate != null && 
               subscriptionEndDate.isAfter(LocalDateTime.now());
    }

    public long getDaysRemaining() {
        if (!hasActiveSubscription()) return 0;
        return java.time.Duration.between(LocalDateTime.now(), subscriptionEndDate).toDays();
    }

    public boolean isSubscriptionExpiringSoon(int days) {
        if (!hasActiveSubscription()) return false;
        return getDaysRemaining() <= days && getDaysRemaining() > 0;
    }

    public void activateSubscription(LocalDateTime startDate, LocalDateTime endDate) {
        this.subscriptionStartDate = startDate;
        this.subscriptionEndDate = endDate;
        this.isSubscriptionActive = true;
        this.status = BusinessStatus.ACTIVE;
    }

    public void deactivateSubscription() {
        this.isSubscriptionActive = false;
        this.status = BusinessStatus.SUSPENDED;
    }
}