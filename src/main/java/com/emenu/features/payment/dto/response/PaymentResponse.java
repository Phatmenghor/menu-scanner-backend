package com.emenu.features.payment.dto.response;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.enums.payment.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PaymentResponse {
    private UUID id;
    private UUID businessId;
    private String businessName;
    private UUID planId;
    private String planName;
    
    // ✅ ADDED: Subscription information
    private UUID subscriptionId;
    private String subscriptionDisplayName;
    
    private BigDecimal amount;
    private BigDecimal amountKhr;
    private String formattedAmount;    // "$29.99"
    private String formattedAmountKhr; // "៛119,960"
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String statusDescription;
    private String referenceNumber;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}