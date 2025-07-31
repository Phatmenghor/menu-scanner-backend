// 1. Update BusinessOwnerCreateRequest.java - Fix phone validation
package com.emenu.features.auth.dto.request;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.enums.payment.PaymentStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class BusinessOwnerCreateRequest {

    // OWNER INFORMATION
    @NotBlank(message = "Owner user identifier is required")
    private String ownerUserIdentifier;
    
    private String ownerEmail;
    
    @NotBlank(message = "Owner password is required")
    @Size(min = 4, max = 100, message = "Owner password must be between 4 and 100 characters")
    private String ownerPassword;

    private String ownerFirstName;
    private String ownerLastName;

    // ✅ FIXED: Simple phone validation - just numbers and spaces
    @Pattern(regexp = "^[0-9\\s]{8,12}$", message = "Phone number should be 8-12 digits (e.g., 070 411260)")
    private String ownerPhone;
    
    private String ownerAddress;

    // BUSINESS INFORMATION
    @NotBlank(message = "Business name is required")
    private String businessName;

    private String businessEmail;
    
    // ✅ FIXED: Simple phone validation - just numbers and spaces
    @Pattern(regexp = "^[0-9\\s]{8,12}$", message = "Phone number should be 8-12 digits (e.g., 070 411260)")
    private String businessPhone; // Optional
    
    private String businessAddress;
    private String businessDescription;

    // SUBDOMAIN INFORMATION - User must provide exact subdomain
    @NotBlank(message = "Subdomain is required")
    @Size(min = 3, max = 63, message = "Subdomain must be between 3 and 63 characters")
    @Pattern(regexp = "^[a-z0-9][a-z0-9-]*[a-z0-9]$", 
             message = "Subdomain can only contain lowercase letters, numbers, and hyphens. Cannot start or end with hyphen")
    private String preferredSubdomain;

    @NotNull(message = "Subscription plan ID is required")
    private UUID subscriptionPlanId;
    
    private LocalDate subscriptionStartDate; // Optional - defaults to today
    private Boolean autoRenew = false;

    // PAYMENT INFORMATION (Optional)
    private String paymentImageUrl;
    @DecimalMin(value = "0.0", message = "Payment amount must be non-negative")
    private BigDecimal paymentAmount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    private String paymentReferenceNumber;
    private String paymentNotes;

    // Helper methods
    public boolean hasSubscriptionInfo() {
        return subscriptionPlanId != null;
    }

    public boolean hasPaymentInfo() {
        return paymentAmount != null && paymentAmount.compareTo(BigDecimal.ZERO) >= 0;
    }

    public boolean isPaymentInfoComplete() {
        return hasPaymentInfo() && paymentMethod != null;
    }
}