package com.emenu.features.subscription.dto.request;

import lombok.Data;

@Data
public class SubscriptionCancelRequest {
    private String reason;
    private String notes;
}