package com.emenu.features.subscription.dto.filter;

import com.emenu.shared.dto.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class SubscriptionFilterRequest extends BaseFilterRequest {
    private UUID businessId;
    private UUID planId;
    private List<UUID> businessIds;
    private List<UUID> planIds;
    private Boolean isActive;
    private Boolean autoRenew;
    
    // Simple date range filtering - only 2 fields
    private LocalDateTime startDate;  // From date
    private LocalDateTime toDate;     // To date
    
    private Boolean expiringSoon;
    private Integer expiringSoonDays = 7;
}
