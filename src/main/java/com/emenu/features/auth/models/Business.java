package com.emenu.features.auth.models;

import com.emenu.enums.user.BusinessStatus;
import com.emenu.features.subscription.models.Subscription;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "businesses")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class Business extends BaseUUIDEntity {

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

    // ✅ ENHANCED: Business Logic Methods with better logging
    public boolean isActive() {
        return BusinessStatus.ACTIVE.equals(status);
    }

    public boolean hasActiveSubscription() {
        log.debug("🔍 Checking active subscription for business: {}", this.getId());
        
        // ✅ ENHANCED: Check both database fields AND active subscriptions
        boolean hasActiveByFields = Boolean.TRUE.equals(isSubscriptionActive) && 
            subscriptionEndDate != null && 
            subscriptionEndDate.isAfter(LocalDateTime.now());
            
        log.debug("📊 Database fields check - isActive: {}, endDate: {}, result: {}", 
                isSubscriptionActive, subscriptionEndDate, hasActiveByFields);

        if (hasActiveByFields) {
            log.debug("✅ Active subscription found via database fields");
            return true;
        }

        // ✅ ENHANCED: Also check subscriptions collection if loaded
        if (subscriptions != null && !subscriptions.isEmpty()) {
            boolean hasActiveInCollection = subscriptions.stream()
                    .anyMatch(sub -> {
                        boolean active = sub.getIsActive() && !sub.isExpired();
                        log.debug("🔍 Subscription {} - active: {}, expired: {}", 
                                sub.getId(), sub.getIsActive(), sub.isExpired());
                        return active;
                    });
            
            log.debug("📊 Collection check - {} subscriptions, active: {}", 
                    subscriptions.size(), hasActiveInCollection);
            
            if (hasActiveInCollection) {
                log.debug("✅ Active subscription found in collection");
                return true;
            }
        } else {
            log.debug("⚠️ Subscriptions collection is null or empty");
        }

        log.debug("❌ No active subscription found for business: {}", this.getId());
        return false;
    }

    public long getDaysRemaining() {
        if (!hasActiveSubscription()) {
            log.debug("❌ No active subscription, returning 0 days remaining");
            return 0;
        }
        
        LocalDateTime endDate = getEffectiveEndDate();
        if (endDate == null) {
            log.debug("⚠️ No effective end date found, returning 0 days remaining");
            return 0;
        }
        
        long days = java.time.Duration.between(LocalDateTime.now(), endDate).toDays();
        long remaining = Math.max(0, days);
        
        log.debug("📊 Days remaining: {} (end date: {})", remaining, endDate);
        return remaining;
    }

    public boolean isSubscriptionExpiringSoon(int days) {
        if (!hasActiveSubscription()) {
            return false;
        }
        long remaining = getDaysRemaining();
        boolean expiringSoon = remaining <= days && remaining > 0;
        
        log.debug("🔍 Expiring soon check - remaining: {}, threshold: {}, result: {}", 
                remaining, days, expiringSoon);
        
        return expiringSoon;
    }

    public void activateSubscription(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("🚀 Activating subscription for business: {} from {} to {}", 
                this.getId(), startDate, endDate);
        
        this.subscriptionStartDate = startDate;
        this.subscriptionEndDate = endDate;
        this.isSubscriptionActive = true;
        this.status = BusinessStatus.ACTIVE;
        
        log.info("✅ Subscription activated for business: {}", this.getId());
    }

    public void deactivateSubscription() {
        log.info("🛑 Deactivating subscription for business: {}", this.getId());
        
        this.isSubscriptionActive = false;
        this.subscriptionStartDate = null;
        this.subscriptionEndDate = null;
        this.status = BusinessStatus.SUSPENDED;
        
        log.info("✅ Subscription deactivated for business: {}", this.getId());
    }

    // ✅ ENHANCED: Helper method to get effective end date with better logic
    private LocalDateTime getEffectiveEndDate() {
        log.debug("🔍 Getting effective end date for business: {}", this.getId());
        
        // First check database field
        if (subscriptionEndDate != null) {
            log.debug("📊 Using database end date: {}", subscriptionEndDate);
            return subscriptionEndDate;
        }
        
        // Then check active subscription
        if (subscriptions != null && !subscriptions.isEmpty()) {
            LocalDateTime latestEndDate = subscriptions.stream()
                    .filter(sub -> {
                        boolean active = sub.getIsActive() && !sub.isExpired();
                        log.debug("🔍 Checking subscription {} - active: {}", sub.getId(), active);
                        return active;
                    })
                    .map(sub -> {
                        log.debug("📊 Subscription {} end date: {}", sub.getId(), sub.getEndDate());
                        return sub.getEndDate();
                    })
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
            
            if (latestEndDate != null) {
                log.debug("📊 Using collection end date: {}", latestEndDate);
                return latestEndDate;
            }
        }
        
        log.debug("❌ No effective end date found");
        return null;
    }
}