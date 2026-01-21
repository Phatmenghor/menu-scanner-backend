package com.emenu.features.order.models;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.enums.payment.PaymentStatus;
import com.emenu.features.auth.models.Business;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "business_order_payments")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class BusinessOrderPayment extends BaseUUIDEntity {

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", insertable = false, updatable = false)
    private Business business;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order order;

    @Column(name = "payment_reference", nullable = false, unique = true)
    private String paymentReference; // Generated reference

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.COMPLETED; // Auto-completed for POS orders

    // Customer payment method for POS orders (e.g., "Cash", "Card", "Mobile Payment")
    @Column(name = "customer_payment_method")
    private String customerPaymentMethod;

    // Business Methods
    public void markAsCompleted() {
        this.status = PaymentStatus.COMPLETED;
    }

    public void markAsFailed() {
        this.status = PaymentStatus.FAILED;
    }

    public void cancel() {
        this.status = PaymentStatus.CANCELLED;
    }

    public boolean isCompleted() {
        return PaymentStatus.COMPLETED.equals(status);
    }

    public boolean isPending() {
        return PaymentStatus.PENDING.equals(status);
    }

    public String getFormattedAmount() {
        return amount != null ? String.format("$%.2f", amount) : "$0.00";
    }

    // Constructor for creating payment
    public BusinessOrderPayment(UUID businessId, UUID orderId, String paymentReference, 
                              BigDecimal amount, PaymentMethod paymentMethod, String customerPaymentMethod) {
        this.businessId = businessId;
        this.orderId = orderId;
        this.paymentReference = paymentReference;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.customerPaymentMethod = customerPaymentMethod;
        this.status = PaymentStatus.COMPLETED; // Auto-complete for POS orders
    }
}