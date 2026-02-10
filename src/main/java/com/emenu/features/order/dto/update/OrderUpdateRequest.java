package com.emenu.features.order.dto.update;

import com.emenu.enums.payment.PaymentMethod;
import lombok.Data;

import java.util.UUID;

@Data
public class OrderUpdateRequest {
    private String orderProcessStatusName;
    private UUID deliveryAddressId;
    private UUID deliveryOptionId;
    private PaymentMethod paymentMethod;
    private String customerNote;
    private String businessNote;
}
