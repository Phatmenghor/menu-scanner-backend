package com.emenu.features.services.domain;

import com.emenu.enums.PaymentStatus;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_records")
@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentRecord extends BaseUUIDEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "subscription_id", nullable = false)
    private UUID subscriptionId;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "payment_method")
    private String paymentMethod; // CREDIT_CARD, PAYPAL, BANK_TRANSFER

    @Column(name = "stripe_payment_intent_id")
    private String stripePaymentIntentId;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "description")
    private String description;

    @Column(name = "invoice_url")
    private String invoiceUrl;

    @Column(name = "failure_reason")
    private String failureReason;

    // Business Methods
    public boolean isSuccessful() {
        return PaymentStatus.COMPLETED.equals(status);
    }

    public boolean isFailed() {
        return PaymentStatus.FAILED.equals(status);
    }
}