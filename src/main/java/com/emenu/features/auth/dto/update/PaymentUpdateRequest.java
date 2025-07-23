package com.emenu.features.auth.dto.update;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.enums.payment.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentUpdateRequest {
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String referenceNumber;
    private String notes;
}