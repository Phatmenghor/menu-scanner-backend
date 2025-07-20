package com.emenu.features.subscription.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.UUID;

@Data
public class PaymentCreateRequest {
    
    @NotNull(message = "Subscription ID is required")
    private UUID subscriptionId;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;
    
    private String paymentMethod = "CREDIT_CARD";
    private String currency = "USD";
    private String description;
}