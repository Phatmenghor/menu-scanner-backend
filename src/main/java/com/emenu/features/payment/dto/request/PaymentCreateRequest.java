package com.emenu.features.payment.dto.request;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.enums.payment.PaymentStatus;
import com.emenu.enums.payment.PaymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PaymentCreateRequest {

    private String imageUrl; // Receipt image URL

    // Option 1: Payment for specific subscription (existing functionality)
    private UUID subscriptionId;
    
    // Option 2: Payment for business directly (for history recording)
    private UUID businessId; // Direct business payment recording
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", message = "Amount must be non-negative")
    private BigDecimal amount;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    private PaymentStatus status = PaymentStatus.PENDING;
    
    private String referenceNumber; // Optional, will auto-generate if not provided
    
    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    // Payment type for different scenarios
    private PaymentType paymentType = PaymentType.SUBSCRIPTION;

    // Helper methods for validation
    public boolean hasSubscriptionInfo() {
        return subscriptionId != null;
    }

    public boolean hasBusinessInfo() {
        return businessId != null;
    }
}