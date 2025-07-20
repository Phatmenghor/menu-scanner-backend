package com.emenu.features.auth.dto.request;

import com.emenu.enums.SubscriptionPlan;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class SubscriptionCreateRequest {
    
    @NotNull(message = "Business ID is required")
    private UUID businessId;
    
    @NotNull(message = "Subscription plan is required")
    private SubscriptionPlan plan;
    
    private Boolean autoRenew = false;
    private String notes;
}