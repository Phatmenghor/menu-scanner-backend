package com.emenu.features.auth.dto.filter;

import com.emenu.enums.PaymentMethod;
import com.emenu.enums.PaymentStatus;
import com.emenu.enums.SubscriptionPlan;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PaymentFilterRequest {
    
    private UUID businessId;
    private UUID subscriptionId;
    private SubscriptionPlan subscriptionPlan;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private LocalDateTime paymentDateFrom;
    private LocalDateTime paymentDateTo;
    private String referenceNumber;
    private String search;
    
    // Pagination
    private Integer pageNo = 1;
    private Integer pageSize = 10;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}