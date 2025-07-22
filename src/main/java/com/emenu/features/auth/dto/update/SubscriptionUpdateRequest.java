package com.emenu.features.auth.dto.update;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SubscriptionUpdateRequest {
    private UUID planId;
    private LocalDateTime endDate;
    private Boolean isActive;
    private Boolean autoRenew;
    private Integer customMaxStaff;
    private Integer customMaxMenuItems;
    private Integer customMaxTables;
    private Integer customDurationDays;
    private String notes;
}