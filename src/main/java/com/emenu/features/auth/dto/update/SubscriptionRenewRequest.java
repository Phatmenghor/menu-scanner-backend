package com.emenu.features.auth.dto.update;

import lombok.Data;

import java.util.UUID;

@Data
public class SubscriptionRenewRequest {
    private UUID newPlanId;
    private Integer customDurationDays;
    private String notes;
}