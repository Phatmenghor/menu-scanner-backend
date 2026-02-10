package com.emenu.features.order.dto.request;

import com.emenu.enums.payment.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class OrderCreateRequest {

    @NotNull(message = "Business ID is required")
    private UUID businessId;

    // Delivery info (optional)
    private UUID deliveryAddressId;
    private UUID deliveryOptionId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private String customerNote;

    private UUID orderProcessStatusId;
}
