package com.emenu.features.order.dto.request;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.enums.payment.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class BusinessOrderPaymentCreateRequest {
    
    @NotNull(message = "Order ID is required")
    private UUID orderId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", message = "Amount must be non-negative")
    private BigDecimal amount;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    private PaymentStatus status = PaymentStatus.PENDING;
    private String confirmationImageUrl;
    private String notes;
    private String customerPaymentMethod;
}