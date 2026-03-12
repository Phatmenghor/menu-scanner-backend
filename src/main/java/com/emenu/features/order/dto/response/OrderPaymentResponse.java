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
public class OrderPaymentResponse extends BaseAuditResponse {
    private UUID businessId;
    private String businessName;
    private UUID orderId;
    private String orderNumber;
    private String paymentReference;

    // Pricing breakdown
    private BigDecimal subtotal;          // Items total before discounts
    private BigDecimal discountAmount;    // Total discount applied
    private BigDecimal deliveryFee;       // Delivery cost
    private BigDecimal taxAmount;         // Tax (null if not applicable)
    private BigDecimal totalAmount;       // Final amount paid
    private String formattedAmount;       // Formatted totalAmount

    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String customerPaymentMethod;

    // Customer info from order
    private String customerName;
    private String customerPhone;
}