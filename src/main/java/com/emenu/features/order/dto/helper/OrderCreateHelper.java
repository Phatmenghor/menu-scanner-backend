package com.emenu.features.order.dto.helper;

import com.emenu.enums.payment.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Helper DTO for creating Order via MapStruct
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateHelper {
    private String orderNumber;
    private UUID customerId;
    private UUID businessId;
    private UUID deliveryAddressId;
    private UUID deliveryOptionId;
    private PaymentMethod paymentMethod;
    private String customerNote;
}
