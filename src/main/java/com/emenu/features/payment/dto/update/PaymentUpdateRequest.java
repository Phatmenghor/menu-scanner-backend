package com.emenu.features.payment.dto.update;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.enums.payment.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentUpdateRequest {

    private String imageUrl;

    @DecimalMin(value = "0.0", message = "Amount must be non-negative")
    private BigDecimal amount;

    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String referenceNumber;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;
}