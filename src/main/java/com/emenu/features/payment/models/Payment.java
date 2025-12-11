package com.emenu.features.payment.models;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.enums.payment.PaymentStatus;
import com.emenu.enums.payment.PaymentType;
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
@Table(name = "payments")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends BaseUUIDEntity {

    private String imageUrl;

    @Column(name = "business_id")
    private UUID businessId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", insertable = false, updatable = false)
    private Business business;

    @Column(name = "plan_id")
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
    private BigDecimal amount;

    @Column(name = "amount_khr", precision = 15, scale = 2)
    private BigDecimal amountKhr;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType = PaymentType.OTHER;

    @Column(name = "reference_number")
    private String referenceNumber;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public void calculateAmountKhr(Double exchangeRate) {
        if (amount != null && exchangeRate != null && exchangeRate > 0) {
            this.amountKhr = amount.multiply(BigDecimal.valueOf(exchangeRate))
                    .setScale(2, RoundingMode.HALF_UP);
        }
    }

    public String getFormattedAmount() {
        return amount != null ? String.format("$%.2f", amount) : "$0.00";
    }

    public String getFormattedAmountKhr() {
        return amountKhr != null ? String.format("៛%.0f", amountKhr) : "៛0";
    }

    public void markAsCompleted() {
        this.status = PaymentStatus.COMPLETED;
    }

    public void markAsFailed() {
        this.status = PaymentStatus.FAILED;
    }

    public void markAsPending() {
        this.status = PaymentStatus.PENDING;
    }

    public String getBusinessName() {
        return business != null ? business.getName() : "Unknown Business";
    }

    public String getPlanName() {
        return plan != null ? plan.getName() : "Unknown Plan";
    }

    public String getSubscriptionDisplayName() {
        return subscription != null ? subscription.getDisplayName() : "No Subscription";
    }

    public boolean hasSubscription() {
        return subscriptionId != null;
    }

    public boolean hasBusiness() {
        return businessId != null;
    }

    public boolean hasPlan() {
        return planId != null;
    }
}