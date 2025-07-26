package com.emenu.features.payment.dto.request;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.enums.payment.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PaymentCreateRequest {

    private String imageUrl; // Receipt image URL
    
    @NotNull(message = "Subscription ID is required")
    private UUID subscriptionId;
    
    private PaymentStatus status = PaymentStatus.PENDING;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    private String referenceNumber; // Optional, will auto-generate if not provided
    
    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;
}
