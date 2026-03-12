package com.emenu.features.order.dto.request;

import com.emenu.features.order.dto.response.CartSummaryResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class OrderCreateRequest {

    @NotNull(message = "Business ID is required")
    private UUID businessId;

    // Delivery info - full objects from frontend (dynamic data)
    private DeliveryAddressRequest deliveryAddress;
    private DeliveryOptionRequest deliveryOption;

    // Cart summary - complete cart data from frontend (can be edited locally before submit)
    private CartSummaryResponse cart;

    @Valid
    @NotNull(message = "Payment info is required")
    private OrderPaymentRequest payment;

    private String customerNote;

    private String orderProcessStatusName;
}
