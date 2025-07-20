package com.emenu.features.subscription.models;

import com.emenu.enums.PaymentStatus;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends BaseUUIDEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private Double amount;

    @Column(name = "currency", length = 3)
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod; // CREDIT_CARD, PAYPAL, BANK_TRANSFER, etc.

    @Column(name = "transaction_id", length = 255)
    private String transactionId;

    @Column(name = "payment_provider", length = 50)
    private String paymentProvider; // STRIPE, PAYPAL, etc.

    @Column(name = "billing_period_start")
    private LocalDateTime billingPeriodStart;

    @Column(name = "billing_period_end")
    private LocalDateTime billingPeriodEnd;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "invoice_number", length = 100)
    private String invoiceNumber;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "refunded_amount", precision = 10, scale = 2)
    private Double refundedAmount = 0.0;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    // Convenience methods
    public boolean isSuccessful() {
        return PaymentStatus.COMPLETED.equals(status);
    }

    public boolean isPending() {
        return PaymentStatus.PENDING.equals(status) || PaymentStatus.PROCESSING.equals(status);
    }

    public boolean isFailed() {
        return PaymentStatus.FAILED.equals(status) || PaymentStatus.CANCELLED.equals(status);
    }

    public boolean isOverdue() {
        return dueDate != null && dueDate.isBefore(LocalDateTime.now()) && !isSuccessful();
    }
}