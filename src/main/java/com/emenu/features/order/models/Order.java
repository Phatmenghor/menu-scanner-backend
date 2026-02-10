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
    private String orderNumber; // Generated unique order number

    // Customer Info - can be null for guest orders
    @Column(name = "customer_id")
    private UUID customerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private User customer;

    // Guest customer info (when no login)
    @Column(name = "guest_phone")
    private String guestPhone; // Required for guest orders

    @Column(name = "guest_name")
    private String guestName;

    @Column(name = "guest_location")
    private String guestLocation; // Simple text location

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", insertable = false, updatable = false)
    private Business business;

    // Delivery info (optional)
    @Column(name = "delivery_address_id")
    private UUID deliveryAddressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_address_id", insertable = false, updatable = false)
    private Location deliveryAddress;

    @Column(name = "delivery_option_id")
    private UUID deliveryOptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_option_id", insertable = false, updatable = false)
    private DeliveryOption deliveryOption;

    @Column(name = "order_process_status_id", nullable = false)
    private UUID orderProcessStatusId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_process_status_id", insertable = false, updatable = false)
    private OrderProcessStatus orderProcessStatus;

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

    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid = false;

    // Order type
    @Column(name = "is_pos_order", nullable = false)
    private Boolean isPosOrder = false; // true when business creates order for customer

    @Column(name = "is_guest_order", nullable = false)
    private Boolean isGuestOrder = false; // true when customer orders without login

    // Timestamps
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items;

    // Business Methods
    public void updateStatus(UUID newOrderProcessStatusId) {
        this.orderProcessStatusId = newOrderProcessStatusId;
    }

    public void confirm() {
        this.confirmedAt = LocalDateTime.now();
    }

    public void complete() {
        this.completedAt = LocalDateTime.now();
    }

    public boolean canBeModified() {
        return orderProcessStatus != null && orderProcessStatus.isActiveType();
    }

    public boolean canBeCancelled() {
        return orderProcessStatus != null &&
               (orderProcessStatus.isActiveType() || orderProcessStatus.isDefault());
    }

    public boolean isCompleted() {
        return orderProcessStatus != null && orderProcessStatus.isCompletedType();
    }

    public boolean isCancelled() {
        return orderProcessStatus != null && orderProcessStatus.isCancelledType();
    }

    public boolean isActive() {
        return orderProcessStatus != null && orderProcessStatus.isActiveType();
    }

    public boolean isGuest() {
        return Boolean.TRUE.equals(isGuestOrder) || customerId == null;
    }

    public boolean isPOS() {
        return Boolean.TRUE.equals(isPosOrder);
    }

    public String getCustomerIdentifier() {
        if (customerId != null && customer != null) {
            return customer.getFullName();
        }
        return guestName != null ? guestName : "Guest Customer";
    }

    public String getCustomerContact() {
        if (customerId != null && customer != null) {
            return customer.getPhoneNumber();
        }
        return guestPhone;
    }

    public void markAsPaid() {
        this.isPaid = true;
    }
}