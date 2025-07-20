package com.emenu.features.auth.dto.request;

import com.emenu.enums.PaymentMethod;
import com.emenu.enums.SubscriptionPlan;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PaymentCreateRequest {
    
    @NotNull(message = "Business ID is required")
    private UUID businessId;
    
    private UUID subscriptionId;
    
    @NotNull(message = "Subscription plan is required")
    private SubscriptionPlan subscriptionPlan;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    private String referenceNumber;
    private String notes;
}