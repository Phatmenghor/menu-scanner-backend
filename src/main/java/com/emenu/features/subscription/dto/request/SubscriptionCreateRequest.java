package com.emenu.features.subscription.dto.request;

import com.emenu.enums.SubscriptionPlan;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class SubscriptionCreateRequest {
    
    private UUID businessId; // Set by controller
    
    @NotNull(message = "Subscription plan is required")
    private SubscriptionPlan plan;
    
    private String billingCycle = "MONTHLY"; // MONTHLY, YEARLY
    private Boolean autoRenew = true;
    
    // Custom limits for enterprise plans
    private Integer customMaxStaff;
    private Integer customMaxMenuItems;
    private Integer customMaxTables;
}
