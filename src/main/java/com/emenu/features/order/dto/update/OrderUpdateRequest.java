package com.emenu.features.order.dto.update;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.enums.payment.PaymentStatus;
import com.emenu.features.order.dto.helper.OrderItemCreateHelper;
import com.emenu.features.order.dto.request.DeliveryAddressRequest;
import com.emenu.features.order.dto.request.DeliveryOptionRequest;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderUpdateRequest {
    private String orderProcessStatusName;
    private DeliveryAddressRequest deliveryAddress;
    private DeliveryOptionRequest deliveryOption;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String customerNote;
    private String businessNote;

    // Full update fields
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal deliveryFee;

    // Items update - allows adding/modifying items
    private List<OrderItemCreateHelper> items;
}
