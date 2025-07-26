package com.emenu.features.payment.dto.response;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.enums.payment.PaymentStatus;
import com.emenu.shared.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class PaymentResponse extends BaseAuditResponse {
    private String imageUrl;
    
    private UUID businessId;
    private String businessName;
    private UUID planId;
    private String planName;
    
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
}