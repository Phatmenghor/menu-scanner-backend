package com.emenu.features.payment.dto.response;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.enums.payment.PaymentStatus;
import com.emenu.shared.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class BusinessOrderPaymentResponse extends BaseAuditResponse {
    private UUID businessId;
    private String businessName;
    private UUID orderId;
    private String orderNumber;
    private String paymentReference;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private LocalDateTime paymentDate;
    private String confirmationImageUrl;
    private String notes;
    private String customerPaymentMethod;
    private String formattedAmount;
}
