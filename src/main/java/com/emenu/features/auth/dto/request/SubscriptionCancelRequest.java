package com.emenu.features.auth.dto.request;

import lombok.Data;

@Data
public class SubscriptionCancelRequest {
    private String reason;
    private String notes;
}