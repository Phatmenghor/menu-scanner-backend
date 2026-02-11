package com.emenu.features.order.dto.update;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.features.order.dto.request.DeliveryAddressRequest;
import com.emenu.features.order.dto.request.DeliveryOptionRequest;
import lombok.Data;

@Data
public class OrderUpdateRequest {
    private String orderProcessStatusName;
    private DeliveryAddressRequest deliveryAddress;
    private DeliveryOptionRequest deliveryOption;
    private PaymentMethod paymentMethod;
    private String customerNote;
    private String businessNote;
}
