package com.emenu.features.order.dto.response;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.enums.payment.PaymentStatus;
import com.emenu.shared.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class OrderResponse extends BaseAuditResponse {
    private String orderNumber;

    // Customer info
    private UUID customerId;
    private String customerName;
    private String customerPhone;

    // Business info
    private UUID businessId;
    private String businessName;

    // Delivery info
    private OrderDeliveryAddressDto deliveryAddress;
    private OrderDeliveryOptionDto deliveryOption;

    // Order details
    private OrderStatusDto orderProcessStatus;
    private String customerNote;
    private String businessNote;

    // Pricing
    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal totalAmount;

    // Payment info
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;

    // Timestamps
    private LocalDateTime confirmedAt;
    private LocalDateTime completedAt;

    // Items
    private List<OrderItemResponse> items;
}
