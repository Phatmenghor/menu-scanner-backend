package com.emenu.features.order.dto.helper;

import com.emenu.enums.payment.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Helper DTO for creating BusinessOrderPayment via MapStruct
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessOrderPaymentCreateHelper {
    private UUID businessId;
    private UUID orderId;
    private String referenceNumber;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private String customerPaymentMethod;
}
