package com.emenu.features.order.dto.update;

import com.emenu.enums.payment.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BusinessOrderPaymentUpdateRequest {
    private BigDecimal amount;
    private PaymentStatus status;
    private String confirmationImageUrl;
    private String notes;
    private String customerPaymentMethod;
}
