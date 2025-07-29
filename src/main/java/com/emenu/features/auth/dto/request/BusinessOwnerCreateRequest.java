package com.emenu.features.auth.dto.request;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.enums.payment.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BusinessOwnerCreateRequest {

    // ✅ OWNER INFORMATION
    @NotBlank(message = "Owner user identifier is required")
    private String ownerUserIdentifier;
    
    private String ownerEmail; // Optional
    
    @NotBlank(message = "Owner password is required")
    @Size(min = 4, max = 100, message = "Owner password must be between 4 and 100 characters")
    private String ownerPassword;

    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    private String ownerFirstName;

    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    private String ownerLastName;

    @Pattern(regexp = "^(\\+855|0)?[1-9][0-9]{7,8}$", message = "Invalid phone number format for Cambodia")
    private String ownerPhone; // Optional
    
    private String ownerAddress;

    // ✅ BUSINESS INFORMATION
    @NotBlank(message = "Business name is required")
    private String businessName;

    private String businessEmail; // Optional
    
    @Pattern(regexp = "^(\\+855|0)?[1-9][0-9]{7,8}$", message = "Invalid phone number format for Cambodia")
    private String businessPhone; // Optional
    
    private String businessAddress;
    private String businessDescription;

    // ✅ SUBDOMAIN INFORMATION
    @NotBlank(message = "Preferred subdomain is required")
    @Size(min = 3, max = 63, message = "Subdomain must be between 3 and 63 characters")
    @Pattern(regexp = "^[a-z0-9][a-z0-9-]*[a-z0-9]$", 
             message = "Subdomain can only contain lowercase letters, numbers, and hyphens. Cannot start or end with hyphen")
    private String preferredSubdomain;

    @NotBlank(message = "SubscriptionPlanId is required")
    private UUID subscriptionPlanId; // Optional - if provided, creates subscription
    private LocalDateTime subscriptionStartDate; // Optional - defaults to now
    private Boolean autoRenew = false;
    private String subscriptionNotes;

    // ✅ NEW: PAYMENT INFORMATION (Optional - if provided, creates payment record)
    private String paymentImageUrl; // Receipt image URL
    @Positive(message = "Payment amount must be positive")
    private BigDecimal paymentAmount; // If provided, creates payment
    private PaymentMethod paymentMethod; // Required if paymentAmount provided
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    private String paymentReferenceNumber; // Optional, will auto-generate if not provided
    private String paymentNotes;

    // ✅ Helper methods for validation
    public boolean hasSubscriptionInfo() {
        return subscriptionPlanId != null;
    }

    public boolean hasPaymentInfo() {
        return paymentAmount != null && paymentAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isPaymentInfoComplete() {
        return hasPaymentInfo() && paymentMethod != null;
    }
}