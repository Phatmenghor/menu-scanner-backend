package com.emenu.features.subscription.dto.update;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SubscriptionUpdateRequest {
    private UUID planId;
    
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    
    private Boolean autoRenew;
}