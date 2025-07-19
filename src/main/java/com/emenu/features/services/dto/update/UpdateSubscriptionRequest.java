package com.emenu.features.services.dto.update;

import com.emenu.enums.SubscriptionStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UpdateSubscriptionRequest {
    private UUID planId;
    private SubscriptionStatus status;
    private LocalDateTime endDate;
    private Boolean autoRenew;
}