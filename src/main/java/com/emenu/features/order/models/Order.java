package com.emenu.features.order.models;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.models.User;
import com.emenu.features.location.models.Location;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseUUIDEntity {

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    // Customer Info
    @Column(name = "customer_id")
    private UUID customerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private User customer;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", insertable = false, updatable = false)
    private Business business;

    // Delivery info - full snapshots from frontend (no IDs)
    @Column(name = "delivery_address_snapshot", columnDefinition = "TEXT")
    private String deliveryAddressSnapshot;

    @Column(name = "delivery_option_name")
    private String deliveryOptionName;

    @Column(name = "delivery_option_description", columnDefinition = "TEXT")
    private String deliveryOptionDescription;

    @Column(name = "order_process_status_name")
    private String orderProcessStatusName;

    @Column(name = "customer_note", columnDefinition = "TEXT")
    private String customerNote;

    @Column(name = "business_note", columnDefinition = "TEXT")
    private String businessNote;

    // Pricing
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "delivery_fee", precision = 10, scale = 2)
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    // Payment info
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "payment_status")
    private String paymentStatus = "UNPAID";

    // Timestamps
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items;

    // Business Methods
    public void updateStatus(String newOrderProcessStatusName) {
        this.orderProcessStatusName = newOrderProcessStatusName;
    }

    public void confirm() {
        this.confirmedAt = LocalDateTime.now();
    }

    public void complete() {
        this.completedAt = LocalDateTime.now();
    }

    public String getCustomerIdentifier() {
        return customer != null ? customer.getFullName() : null;
    }

    public String getCustomerContact() {
        return customer != null ? customer.getPhoneNumber() : null;
    }

    public void markAsPaid() {
        this.paymentStatus = "PAID";
    }

    public void markAsUnpaid() {
        this.paymentStatus = "UNPAID";
    }

    public void markAsPartiallyPaid() {
        this.paymentStatus = "PARTIALLY_PAID";
    }

    public void markAsRefunded() {
        this.paymentStatus = "REFUNDED";
    }
}
