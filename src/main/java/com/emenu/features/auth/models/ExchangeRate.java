package com.emenu.features.auth.models;

import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exchange_rates")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRate extends BaseUUIDEntity {

    @Column(name = "usd_to_khr_rate", nullable = false)
    private Double usdToKhrRate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "notes")
    private String notes;

    // ================================
    // BUSINESS METHODS
    // ================================

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
        return "System Exchange Rate";
    }

    /**
     * Format the rate for display
     */
    public String getFormattedRate() {
        return String.format("1 USD = %.0f KHR", usdToKhrRate);
    }
}