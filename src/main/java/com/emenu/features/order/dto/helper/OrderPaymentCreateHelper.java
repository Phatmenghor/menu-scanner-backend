package com.emenu.features.order.dto.helper;

import com.emenu.enums.payment.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Helper DTO for creating OrderPayment via MapStruct
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaymentCreateHelper {
    private UUID businessId;
    private UUID orderId;
    private String referenceNumber;

    // Pricing breakdown
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal deliveryFee;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;

    private PaymentMethod paymentMethod;
    private String customerPaymentMethod;
}
