package com.emenu.features.order.dto.response;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.features.location.dto.response.LocationResponse;
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
    private LocationResponse deliveryAddress;
    private DeliveryOptionResponse deliveryOption;

    // Order details
    private OrderProcessStatusResponse orderProcessStatus;
    private String customerNote;
    private String businessNote;

    // Pricing
    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal totalAmount;

    // Payment info
    private PaymentMethod paymentMethod;
    private Boolean isPaid;

    // Timestamps
    private LocalDateTime confirmedAt;
    private LocalDateTime completedAt;

    // Items
    private List<OrderItemResponse> items;
}
