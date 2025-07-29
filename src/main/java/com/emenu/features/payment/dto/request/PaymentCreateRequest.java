package com.emenu.features.payment.dto.request;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.enums.payment.PaymentStatus;
import com.emenu.enums.payment.PaymentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PaymentCreateRequest {

    private String imageUrl; // Receipt image URL

    // Option 1: Traditional - Payment for specific subscription (existing functionality)
    private UUID subscriptionId;
    
    // Option 2: NEW - Payment for user with plan (dynamic selection)
    private UUID userId; // Select user first
    private UUID planId; // Then select plan for that user
    
    // Option 3: NEW - Payment for business directly (for history recording)
    private UUID businessId; // Direct business payment recording
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    private PaymentStatus status = PaymentStatus.PENDING;
    
    private String referenceNumber; // Optional, will auto-generate if not provided
    
    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    // ✅ NEW: Payment type for different scenarios
    private PaymentType paymentType = PaymentType.SUBSCRIPTION; // Default to existing behavior

    // ✅ Helper methods for validation
    public boolean hasSubscriptionInfo() {
        return subscriptionId != null;
    }

    public boolean hasUserPlanInfo() {
        return userId != null && planId != null;
    }

    public boolean hasBusinessInfo() {
        return businessId != null;
    }

    public boolean isValidPaymentRequest() {
        // Must have exactly one way to identify what the payment is for
        int methods = 0;
        if (hasSubscriptionInfo()) methods++;
        if (hasUserPlanInfo()) methods++;
        if (hasBusinessInfo()) methods++;
        
        return methods == 1;
    }

}