package com.emenu.features.payment.dto.filter;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.enums.payment.PaymentStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PaymentFilterRequest {
    private String search; // Global search
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private UUID businessId;
    private UUID planId;

    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;
    
    @Min(value = 1, message = "Page number must be at least 1")
    private Integer pageNo = 1;
    
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    private Integer pageSize = 10;
    
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}