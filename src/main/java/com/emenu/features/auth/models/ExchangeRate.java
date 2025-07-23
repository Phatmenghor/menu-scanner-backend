package com.emenu.features.auth.models;

import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "exchange_rates")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRate extends BaseUUIDEntity {

    @Column(name = "business_id")
    private UUID businessId; // NULL means system default

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", insertable = false, updatable = false)
    private Business business;

    @Column(name = "usd_to_khr_rate", nullable = false)
    private Double usdToKhrRate;

    @Column(name = "is_system_default", nullable = false)
    private Boolean isSystemDefault = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "notes")
    private String notes;

    // ================================
    // BUSINESS METHODS
    // ================================

    /**
     * Check if this is the system default rate
     */
    public boolean isSystemDefault() {
        return Boolean.TRUE.equals(isSystemDefault) && businessId == null;
    }

    /**
     * Check if this rate is currently active
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    /**
     * Get display name for this rate
     */
    public String getDisplayName() {
        if (isSystemDefault()) {
            return "System Default Rate";
        } else if (business != null) {
            return business.getName() + " Rate";
        } else {
            return "Unknown Rate";
        }
    }

    /**
     * Format the rate for display
     */
    public String getFormattedRate() {
        return String.format("1 USD = %.0f KHR", usdToKhrRate);
    }
}