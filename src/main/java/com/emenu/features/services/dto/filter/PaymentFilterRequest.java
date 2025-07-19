package com.emenu.features.services.dto.filter;

import com.emenu.enums.PaymentStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class PaymentFilterRequest {
    private String search; // User email
    private UUID subscriptionId;
    private PaymentStatus status;
    private String paymentMethod;
    private LocalDate paymentDateAfter;
    private LocalDate paymentDateBefore;
    
    // Pagination
    private Integer pageNo = 0;
    private Integer pageSize = 10;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}
