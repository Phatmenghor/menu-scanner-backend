package com.emenu.features.auth.dto.filter;

import com.emenu.enums.PaymentMethod;
import com.emenu.enums.PaymentStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class PaymentFilterRequest {
    
    // Business and Plan filters
    private UUID businessId;
    private List<UUID> businessIds;
    private UUID subscriptionId;
    private UUID planId;
    private List<UUID> planIds;
    
    // Payment details filters
    private PaymentMethod paymentMethod;
    private List<PaymentMethod> paymentMethods;
    private PaymentStatus status;
    private List<PaymentStatus> statuses;
    
    // Amount filters
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    
    // Date filters
    private LocalDateTime paymentDateFrom;
    private LocalDateTime paymentDateTo;
    private LocalDateTime dueDateFrom;
    private LocalDateTime dueDateTo;
    private LocalDateTime createdDateFrom;
    private LocalDateTime createdDateTo;
    
    // Status filters
    private Boolean isOverdue;
    private Boolean isCompleted;
    private Boolean isPending;
    private Boolean isFailed;
    
    // Search filters
    private String referenceNumber;
    private String externalTransactionId;
    private String search; // Global search across multiple fields
    
    // Processed by filter
    private UUID processedBy;
    
    // Currency filter
    private String currency;
    
    // Pagination
    @Min(value = 1, message = "Page number must be at least 1")
    private Integer pageNo = 1;
    
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    private Integer pageSize = 10;
    
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}