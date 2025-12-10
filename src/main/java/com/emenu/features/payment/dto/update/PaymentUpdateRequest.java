package com.emenu.features.payment.dto.update;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.enums.payment.PaymentStatus;
import com.emenu.enums.payment.PaymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PaymentUpdateRequest {

    private String imageUrl;
    private UUID subscriptionId;
    private UUID businessId;

    @DecimalMin(value = "0.0", message = "Amount must be non-negative")
    private BigDecimal amount;

    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String referenceNumber;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;
    private PaymentType paymentType;
}