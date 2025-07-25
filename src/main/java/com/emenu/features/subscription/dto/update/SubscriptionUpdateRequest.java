package com.emenu.features.subscription.dto.update;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SubscriptionUpdateRequest {
    private UUID planId;
    
    // Admin can edit both start and end dates
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    
    private Boolean isActive;
    private Boolean autoRenew;
    private String notes;
}