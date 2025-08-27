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
    private Boolean isActive;
    private Boolean autoRenew;
    private LocalDateTime startDate;
    private LocalDateTime toDate;
    private Integer expiringSoonDays = 7;
}
