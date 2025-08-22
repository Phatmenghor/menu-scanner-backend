package com.emenu.features.notification.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class SubscriptionAlertDto {
    private UUID businessId;
    private String businessName;
    private int daysUntilExpiry;
    private String alertType; // EXPIRING_SOON, EXPIRED, etc.
}