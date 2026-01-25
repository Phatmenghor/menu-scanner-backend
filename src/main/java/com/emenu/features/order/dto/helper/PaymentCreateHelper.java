package com.emenu.features.order.dto.helper;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.enums.payment.PaymentStatus;
import com.emenu.enums.payment.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Helper DTO for creating Payment via MapStruct
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreateHelper {
    private UUID businessId;
    private UUID planId;
    private UUID subscriptionId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentType paymentType;
    private PaymentStatus status;
    private String notes;
}
