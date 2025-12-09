package com.emenu.features.payment.dto.request;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.enums.payment.PaymentStatus;
import com.emenu.enums.payment.PaymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PaymentCreateRequest {

    private String imageUrl;
    private UUID subscriptionId;
    private UUID businessId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", message = "Amount must be non-negative")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private PaymentStatus status = PaymentStatus.PENDING;
    private String referenceNumber;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    private PaymentType paymentType = PaymentType.SUBSCRIPTION;

    public boolean hasSubscriptionInfo() {
        return subscriptionId != null;
    }

    public boolean hasBusinessInfo() {
        return businessId != null;
    }
}