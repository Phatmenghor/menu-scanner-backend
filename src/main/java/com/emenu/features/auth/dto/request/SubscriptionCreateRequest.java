package com.emenu.features.auth.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class SubscriptionCreateRequest {
    @NotNull(message = "Business ID is required")
    private UUID businessId;
    
    @NotNull(message = "Plan ID is required")
    private UUID planId;
    
    private Boolean autoRenew = false;
    private Integer customMaxStaff;
    private Integer customMaxMenuItems;
    private Integer customMaxTables;
    private Integer customDurationDays;
    private String notes;
    private Boolean isTrial = false;
}