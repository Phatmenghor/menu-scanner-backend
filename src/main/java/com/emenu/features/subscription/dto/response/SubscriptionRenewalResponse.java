package com.emenu.features.subscription.dto.response;

import com.emenu.enums.payment.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SubscriptionRenewalResponse {
    private SubscriptionResponse subscription;
    private Boolean paymentCreated;
    private BigDecimal paymentAmount;
    private PaymentMethod paymentMethod;
    
    // Summary info
    private String renewalSummary;
    private LocalDateTime newEndDate;
    private Integer addedDays;
}