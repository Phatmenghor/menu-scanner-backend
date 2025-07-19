package com.emenu.features.services.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreatePaymentRequest {
    @NotNull
    private UUID subscriptionId;
    
    @NotNull
    @Positive
    private BigDecimal amount;
    
    private String currency = "USD";
    private String paymentMethod;
    private String description;
}