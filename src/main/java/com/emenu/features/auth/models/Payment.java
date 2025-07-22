package com.emenu.features.auth.models;

import com.emenu.enums.PaymentMethod;
import com.emenu.enums.PaymentStatus;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends BaseUUIDEntity {

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", insertable = false, updatable = false)
    private Business business;

    @Column(name = "subscription_id")
    private UUID subscriptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", insertable = false, updatable = false)
    private Subscription subscription;

    @Column(name = "plan_id", nullable = false)
    private UUID planId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", insertable = false, updatable = false)
    private SubscriptionPlan plan;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "amount_khr", precision = 15, scale = 2)
    private BigDecimal amountKhr; // Amount in Cambodian Riel

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "reference_number", unique = true)
    private String referenceNumber;

    @Column(name = "external_transaction_id")
    private String externalTransactionId; // For future payment gateway integration

    @Column(name = "currency", length = 3)
    private String currency = "USD";

    @Column(name = "exchange_rate")
    private Double exchangeRate; // USD to KHR rate at time of payment

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes; // Internal admin notes

    @Column(name = "payment_proof_url")
    private String paymentProofUrl; // URL to payment proof image

    @Column(name = "processed_by")
    private UUID processedBy; // Admin who processed the payment

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    // Business Logic Methods
    public boolean isCompleted() {
        return PaymentStatus.COMPLETED.equals(status);
    }

    public boolean isPending() {
        return PaymentStatus.PENDING.equals(status);
    }

    public boolean isFailed() {
        return PaymentStatus.FAILED.equals(status);
    }

    public boolean isRefunded() {
        return PaymentStatus.REFUNDED.equals(status);
    }

    public boolean isOverdue() {
        return dueDate != null && LocalDateTime.now().isAfter(dueDate) && isPending();
    }

    public void markAsCompleted(UUID processedBy) {
        this.status = PaymentStatus.COMPLETED;
        this.paymentDate = LocalDateTime.now();
        this.processedBy = processedBy;
        this.processedAt = LocalDateTime.now();
    }

    public void markAsFailed(UUID processedBy, String reason) {
        this.status = PaymentStatus.FAILED;
        this.processedBy = processedBy;
        this.processedAt = LocalDateTime.now();
        if (reason != null) {
            this.adminNotes = (this.adminNotes != null ? this.adminNotes + "\n" : "") + "Failed: " + reason;
        }
    }

    public void markAsRefunded(UUID processedBy, String reason) {
        this.status = PaymentStatus.REFUNDED;
        this.processedBy = processedBy;
        this.processedAt = LocalDateTime.now();
        if (reason != null) {
            this.adminNotes = (this.adminNotes != null ? this.adminNotes + "\n" : "") + "Refunded: " + reason;
        }
    }

    // Calculate KHR amount based on exchange rate
    public void calculateAmountKhr(Double exchangeRate) {
        if (amount != null && exchangeRate != null) {
            this.exchangeRate = exchangeRate;
            this.amountKhr = amount.multiply(BigDecimal.valueOf(exchangeRate));
        }
    }

    // Helper methods for business logic
    public String getPlanName() {
        return plan != null ? plan.getName() : "Unknown Plan";
    }

    public String getBusinessName() {
        return business != null ? business.getName() : "Unknown Business";
    }

    public String getFormattedAmount() {
        return String.format("$%.2f", amount);
    }

    public String getFormattedAmountKhr() {
        return amountKhr != null ? String.format("៛%.0f", amountKhr) : "N/A";
    }

    // Payment duration calculation
    public long getDaysUntilDue() {
        if (dueDate == null) return 0;
        return java.time.Duration.between(LocalDateTime.now(), dueDate).toDays();
    }
}