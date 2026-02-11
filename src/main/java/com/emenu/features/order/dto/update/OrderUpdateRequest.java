package com.emenu.features.order.dto.update;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.features.order.dto.request.DeliveryAddressRequest;
import com.emenu.features.order.dto.request.DeliveryOptionRequest;
import lombok.Data;

import java.util.UUID;

@Data
public class OrderUpdateRequest {
    private UUID orderProcessStatusId;
    private DeliveryAddressRequest deliveryAddress;
    private DeliveryOptionRequest deliveryOption;
    private PaymentMethod paymentMethod;
    private String customerNote;
    private String businessNote;
}
