package com.emenu.features.auth.dto.response;

import com.emenu.enums.PaymentMethod;
import com.emenu.enums.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PaymentResponse {
    
    private UUID id;
    private UUID businessId;
    private String businessName;
    private UUID subscriptionId;
    
    // Plan information
    private UUID planId;
    private String planName;
    private String planDisplayName;
    
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private LocalDateTime paymentDate;
    private String referenceNumber;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}