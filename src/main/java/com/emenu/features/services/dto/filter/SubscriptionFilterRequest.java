package com.emenu.features.services.dto.filter;

import com.emenu.enums.SubscriptionStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class SubscriptionFilterRequest {
    private String search; // User email/name
    private UUID planId;
    private SubscriptionStatus status;
    private Boolean autoRenew;
    private LocalDate startDateAfter;
    private LocalDate startDateBefore;
    private LocalDate endDateAfter;
    private LocalDate endDateBefore;
    
    // Pagination
    private Integer pageNo = 0;
    private Integer pageSize = 10;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}
