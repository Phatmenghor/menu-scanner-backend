package com.emenu.features.auth.dto.update;

import com.emenu.enums.PaymentMethod;
import com.emenu.enums.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentUpdateRequest {
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private LocalDateTime dueDate;
    private String referenceNumber;
    private String externalTransactionId;
    private Double exchangeRate;
    private String notes;
    private String adminNotes;
    private String paymentProofUrl;
}