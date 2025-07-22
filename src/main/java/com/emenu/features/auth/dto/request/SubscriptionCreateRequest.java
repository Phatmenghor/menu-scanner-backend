package com.emenu.features.auth.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SubscriptionCreateRequest {
    @NotNull(message = "Business ID is required")
    private UUID businessId;
    
    @NotNull(message = "Plan ID is required")
    private UUID planId;
    
    // Admin can set when subscription starts (if null, starts now)
    private LocalDateTime startDate;
    
    private Boolean autoRenew = false;
    private String notes;
}