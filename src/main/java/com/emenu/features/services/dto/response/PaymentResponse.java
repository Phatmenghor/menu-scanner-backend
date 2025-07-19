package com.emenu.features.services.dto.response;

import com.emenu.enums.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PaymentResponse {
    private UUID id;
    private UUID userId;
    private String userEmail;
    private UUID subscriptionId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String paymentMethod;
    private String transactionId;
    private LocalDateTime paymentDate;
    private String description;
    private String invoiceUrl;
    private String failureReason;
    private LocalDateTime createdAt;
}