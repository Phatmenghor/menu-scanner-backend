package com.emenu.features.auth.dto.request;

import com.emenu.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PaymentCreateRequest {
    
    @NotNull(message = "Business ID is required")
    private UUID businessId;
    
    private UUID subscriptionId;
    
    @NotNull(message = "Subscription plan ID is required")
    private UUID planId;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    private LocalDateTime dueDate;
    
    private String referenceNumber;
    
    private String externalTransactionId;
    
    private String currency = "USD";
    
    private Double exchangeRate;
    
    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;
    
    @Size(max = 1000, message = "Admin notes cannot exceed 1000 characters")
    private String adminNotes;
    
    private String paymentProofUrl;
    
    // Auto-complete for free plans
    private Boolean autoComplete = false;
}