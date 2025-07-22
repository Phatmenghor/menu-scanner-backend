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
    private UUID planId;
    private String planName;
    
    private BigDecimal amount;
    private BigDecimal amountKhr;
    private String formattedAmount;
    private String formattedAmountKhr;
    
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String statusDescription;
    
    private LocalDateTime paymentDate;
    private LocalDateTime dueDate;
    private Boolean isOverdue;
    private Long daysUntilDue;
    
    private String referenceNumber;
    private String externalTransactionId;
    private String currency;
    private Double exchangeRate;
    
    private String notes;
    private String adminNotes;
    private String paymentProofUrl;
    
    private UUID processedBy;
    private String processedByName;
    private LocalDateTime processedAt;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}