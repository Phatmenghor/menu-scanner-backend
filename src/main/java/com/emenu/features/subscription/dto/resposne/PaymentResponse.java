package com.emenu.features.subscription.dto.resposne;

import com.emenu.enums.PaymentStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PaymentResponse {
    
    private UUID id;
    private UUID subscriptionId;
    private Double amount;
    private String currency;
    private PaymentStatus status;
    private String paymentMethod;
    private String transactionId;
    private String invoiceNumber;
    private LocalDateTime dueDate;
    private LocalDateTime paidAt;
    private String description;
    private LocalDateTime createdAt;
}