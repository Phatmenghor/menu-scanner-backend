package com.emenu.features.order.dto.response;

import com.emenu.enums.order.OrderStatus;
import com.emenu.features.customer.dto.response.CustomerAddressResponse;
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
    private UUID customerId;
    private String customerName;
    private UUID businessId;
    private String businessName;
    private CustomerAddressResponse deliveryAddress;
    private DeliveryOptionResponse deliveryOption;
    private OrderStatus status;
    private String customerNote;
    private String businessNote;
    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal totalAmount;
    private LocalDateTime confirmedAt;
    private LocalDateTime preparedAt;
    private LocalDateTime deliveredAt;
    private List<OrderItemResponse> items;
    private Boolean canBeModified;
    private Boolean canBeCancelled;
}
