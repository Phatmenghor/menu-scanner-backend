package com.emenu.features.payment.models;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.enums.payment.PaymentStatus;
import com.emenu.features.auth.models.Business;
import com.emenu.features.subscription.models.Subscription;
import com.emenu.features.subscription.models.SubscriptionPlan;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Entity
@Table(name = "payment_service", indexes = {
        // ✅ FIXED: BaseUUIDEntity indexes
        @Index(name = "idx_payment_service_deleted", columnList = "is_deleted"),
        @Index(name = "idx_payment_service_deleted_created", columnList = "is_deleted, created_at"),
        @Index(name = "idx_payment_service_deleted_updated", columnList = "is_deleted, updated_at"),

        // ✅ FIXED: Payment management indexes with unique names
        @Index(name = "idx_payment_service_business_deleted", columnList = "business_id, is_deleted"),
        @Index(name = "idx_payment_service_plan_deleted", columnList = "plan_id, is_deleted"),
        @Index(name = "idx_payment_service_subscription_deleted", columnList = "subscription_id, is_deleted"),
        @Index(name = "idx_payment_service_status_deleted", columnList = "status, is_deleted"),
        @Index(name = "idx_payment_service_method_deleted", columnList = "payment_method, is_deleted"),
        @Index(name = "idx_payment_service_business_status_deleted", columnList = "business_id, status, is_deleted"),
        @Index(name = "idx_payment_service_reference_deleted", columnList = "reference_number, is_deleted"),
        @Index(name = "idx_payment_service_amount_deleted", columnList = "amount, is_deleted")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends BaseUUIDEntity {

    private String imageUrl;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", insertable = false, updatable = false)
    private Business business;

    @Column(name = "plan_id", nullable = false)
    private UUID planId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", insertable = false, updatable = false)
    private SubscriptionPlan plan;

    @Column(name = "subscription_id")
    private UUID subscriptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", insertable = false, updatable = false)
    private Subscription subscription;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount; // Amount in USD

    @Column(name = "amount_khr", precision = 15, scale = 2)
    private BigDecimal amountKhr; // Amount in KHR (calculated)

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    // ✅ FIXED: Removed unique = true constraint to allow duplicate reference numbers
    @Column(name = "reference_number")
    private String referenceNumber;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // ================================
    // BUSINESS METHODS
    // ================================

    /**
     * Calculate KHR amount using exchange rate
     */
    public void calculateAmountKhr(Double exchangeRate) {
        if (amount != null && exchangeRate != null && exchangeRate > 0) {
            this.amountKhr = amount.multiply(BigDecimal.valueOf(exchangeRate))
                    .setScale(2, RoundingMode.HALF_UP);
        }
    }

    /**
     * Get formatted USD amount
     */
    public String getFormattedAmount() {
        return amount != null ? String.format("$%.2f", amount) : "$0.00";
    }

    /**
     * Get formatted KHR amount
     */
    public String getFormattedAmountKhr() {
        return amountKhr != null ? String.format("៛%.0f", amountKhr) : "៛0";
    }

    /**
     * Mark payment as completed
     */
    public void markAsCompleted() {
        this.status = PaymentStatus.COMPLETED;
    }

    /**
     * Get business name safely
     */
    public String getBusinessName() {
        return business != null ? business.getName() : "Unknown Business";
    }

    /**
     * Get plan name safely
     */
    public String getPlanName() {
        return plan != null ? plan.getName() : "Unknown Plan";
    }

    /**
     * Get subscription display name safely
     */
    public String getSubscriptionDisplayName() {
        return subscription != null ? subscription.getDisplayName() : "No Subscription";
    }
}