package com.emenu.features.subscription.dto.filter;

import com.emenu.shared.dto.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class SubscriptionFilterRequest extends BaseFilterRequest {
    private UUID businessId;
    private UUID planId;
    private Boolean autoRenew;
    private LocalDateTime startDate;
    private LocalDateTime toDate;

    private String status;  // ACTIVE, EXPIRED, EXPIRING_SOON, or null for ALL
    private Integer expiringSoonDays = 7;  // Only used when status = EXPIRING_SOON
}