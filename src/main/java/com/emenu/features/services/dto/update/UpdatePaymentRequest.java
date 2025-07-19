package com.emenu.features.services.dto.update;

import com.emenu.enums.PaymentStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdatePaymentRequest {
    private PaymentStatus status;
    private String transactionId;
    private String failureReason;
    private LocalDateTime paymentDate;
    private String invoiceUrl;
}
