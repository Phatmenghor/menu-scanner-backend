package com.emenu.features.order.dto.response;

import com.emenu.enums.order.OrderStatus;
import com.emenu.enums.payment.PaymentMethod;
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
    
    // Customer info
    private UUID customerId;
    private String customerName;
    private String customerPhone;
    private String customerLocation;
    
    // Business info
    private UUID businessId;
    private String businessName;
    
    // Delivery info
    private CustomerAddressResponse deliveryAddress;
    private DeliveryOptionResponse deliveryOption;
    
    // Order details
    private OrderStatus status;
    private String customerNote;
    private String businessNote;
    
    // Pricing
    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal totalAmount;
    
    // Payment info
    private PaymentMethod paymentMethod;
    private String customerPaymentMethod;
    private Boolean isPaid;
    
    // Order type
    private Boolean isPosOrder;
    private Boolean isGuestOrder;
    
    // Timestamps
    private LocalDateTime confirmedAt;
    private LocalDateTime completedAt;
    
    // Items
    private List<OrderItemResponse> items;
    
    // Business logic
    private Boolean canBeModified;
    private Boolean canBeCancelled;
}