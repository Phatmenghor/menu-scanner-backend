package com.emenu.features.auth.dto.update;

import com.emenu.enums.SubscriptionPlan;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubscriptionUpdateRequest {
    
    private SubscriptionPlan plan;
    private LocalDateTime endDate;
    private Boolean isActive;
    private Boolean autoRenew;
    private String notes;
}
