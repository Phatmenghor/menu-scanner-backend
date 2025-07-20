package com.emenu.features.auth.dto.filter;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SubscriptionFilterRequest {
    
    private UUID businessId;
    private UUID planId;
    private Boolean isActive;
    private Boolean autoRenew;
    private LocalDateTime startDateFrom;
    private LocalDateTime startDateTo;
    private LocalDateTime endDateFrom;
    private LocalDateTime endDateTo;
    private Boolean isExpired;
    private Boolean expiringSoon;
    private Boolean isTrial;
    
    // Pagination
    private Integer pageNo = 1;
    private Integer pageSize = 10;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}