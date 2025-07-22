package com.emenu.features.auth.dto.filter;

import com.emenu.enums.sub_scription.SubscriptionStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class SubscriptionFilterRequest {
    private String search;
    private UUID businessId;
    private UUID planId;
    private List<UUID> businessIds;
    private List<UUID> planIds;
    private SubscriptionStatus status;
    private List<SubscriptionStatus> statuses;
    private Boolean autoRenew;
    private LocalDateTime startDateFrom;
    private LocalDateTime startDateTo;
    private LocalDateTime endDateFrom;
    private LocalDateTime endDateTo;
    private Boolean expiringSoon;
    private Integer expiringSoonDays = 7;
    private Boolean hasCustomLimits;

    @Min(value = 1, message = "Page number must be at least 1")
    private Integer pageNo = 1;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    private Integer pageSize = 10;

    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}