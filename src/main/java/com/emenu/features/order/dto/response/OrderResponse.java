package com.emenu.features.order.dto.response;

import com.emenu.shared.dto.BaseAuditResponse;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
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

    // Pricing breakdown - nested object for cleaner structure
    @Valid
    private OrderPricingInfo pricing;

    // Payment info - nested object
    @Valid
    private OrderPaymentInfo payment;

    // Items
    private List<OrderItemResponse> items;

    // Status history
    private List<OrderStatusHistoryResponse> statusHistory;
}
