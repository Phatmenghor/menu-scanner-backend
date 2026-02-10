package com.emenu.features.order.dto.request;

import com.emenu.enums.payment.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class OrderCreateRequest {

    @NotNull(message = "Business ID is required")
    private UUID businessId;

    // Delivery info (optional) - can be ID or full object
    private UUID deliveryAddressId;
    private DeliveryAddressRequest deliveryAddress;

    private UUID deliveryOptionId;
    private DeliveryOptionRequest deliveryOption;

    // Cart items (if provided, will be used instead of fetching from cart)
    private List<CartItemRequest> items;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private String customerNote;

    private String orderProcessStatusName;
}
