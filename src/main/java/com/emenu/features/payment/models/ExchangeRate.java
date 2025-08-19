package com.emenu.features.payment.models;

import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exchange_rates", indexes = {
        // ✅ FIXED: BaseUUIDEntity indexes
        @Index(name = "idx_exchange_rate_deleted", columnList = "is_deleted"),
        @Index(name = "idx_exchange_rate_deleted_created", columnList = "is_deleted, created_at"),

        // ✅ FIXED: Exchange rate management
        @Index(name = "idx_exchange_rate_active_deleted", columnList = "is_active, is_deleted"),
        @Index(name = "idx_exchange_rate_active_created_deleted", columnList = "is_active, created_at, is_deleted"),
        @Index(name = "idx_exchange_rate_usd_khr_deleted", columnList = "usd_to_khr_rate, is_deleted")
})
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
}