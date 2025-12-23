package com.emenu.features.payment.models;

import com.emenu.features.auth.models.Business;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "business_exchange_rates", indexes = {
        @Index(name = "idx_business_exchange_rate_business", columnList = "business_id, is_deleted"),
        @Index(name = "idx_business_exchange_rate_active", columnList = "business_id, is_active, is_deleted")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class BusinessExchangeRate extends BaseUUIDEntity {

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", insertable = false, updatable = false)
    private Business business;

    // Required: USD to KHR exchange rate
    @Column(name = "usd_to_khr_rate", nullable = false)
    private Double usdToKhrRate;

    // Optional: USD to Chinese Yuan (CNY)
    @Column(name = "usd_to_cny_rate")
    private Double usdToCnyRate;

    // Optional: USD to Thai Baht (THB)
    @Column(name = "usd_to_thb_rate")
    private Double usdToThbRate;

    // Optional: USD to Vietnamese Dong (VND)
    @Column(name = "usd_to_vnd_rate")
    private Double usdToVndRate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // ================================
    // BUSINESS METHODS
    // ================================

    /**
     * Check if this rate is currently active for the business
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    /**
     * Activate this exchange rate
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Deactivate this exchange rate
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Check if optional currency rates are set
     */
    public boolean hasCnyRate() {
        return usdToCnyRate != null && usdToCnyRate > 0;
    }

    public boolean hasThbRate() {
        return usdToThbRate != null && usdToThbRate > 0;
    }

    public boolean hasVndRate() {
        return usdToVndRate != null && usdToVndRate > 0;
    }

    /**
     * Get formatted rates for display
     */
    public String getFormattedKhrRate() {
        return String.format("1 USD = %.2f KHR", usdToKhrRate);
    }

    public String getFormattedCnyRate() {
        return hasCnyRate() ? String.format("1 USD = %.4f CNY", usdToCnyRate) : "Not set";
    }

    public String getFormattedThbRate() {
        return hasThbRate() ? String.format("1 USD = %.4f THB", usdToThbRate) : "Not set";
    }

    public String getFormattedVndRate() {
        return hasVndRate() ? String.format("1 USD = %.2f VND", usdToVndRate) : "Not set";
    }
}


