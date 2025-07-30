package com.emenu.features.subscription.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class SubscriptionCreateRequest {
    @NotNull(message = "Business ID is required")
    private UUID businessId;
    
    @NotNull(message = "Plan ID is required")
    private UUID planId;
    
    // Use LocalDate instead of LocalDateTime - defaults to today
    private LocalDate startDate;
    
    private Boolean autoRenew = false;
}