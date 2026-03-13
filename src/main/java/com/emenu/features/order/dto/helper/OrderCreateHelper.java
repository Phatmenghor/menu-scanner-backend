package com.emenu.features.order.dto.helper;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.enums.payment.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String customerNote;

    // Delivery snapshots - full JSON from frontend
    private String deliveryAddressSnapshot;
    private String deliveryOptionSnapshot;
    private BigDecimal deliveryFee;
}
