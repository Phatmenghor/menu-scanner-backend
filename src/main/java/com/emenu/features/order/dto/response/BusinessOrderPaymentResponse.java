package com.emenu.features.order.dto.response;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.enums.payment.PaymentStatus;
import com.emenu.shared.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
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
    private String formattedAmount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String customerPaymentMethod;
    
    // Customer info from order
    private String customerName;
    private String customerPhone;
    private Boolean isGuestOrder;
    private Boolean isPosOrder;
}