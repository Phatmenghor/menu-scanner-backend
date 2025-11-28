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

    @NotBlank(message = "Owner user identifier is required")
    private String ownerUserIdentifier;
    
    private String ownerEmail;
    
    @NotBlank(message = "Owner password is required")
    @Size(min = 4, max = 100)
    private String ownerPassword;

    private String ownerFirstName;
    private String ownerLastName;

    @Pattern(regexp = "^[0-9\\s]{8,12}$", message = "Phone number should be 8-12 digits")
    private String ownerPhone;
    
    private String ownerAddress;

    @NotBlank(message = "Business name is required")
    private String businessName;

    private String businessEmail;
    
    @Pattern(regexp = "^[0-9\\s]{8,12}$", message = "Phone number should be 8-12 digits")
    private String businessPhone;
    
    private String businessAddress;
    private String businessDescription;

    @NotBlank(message = "Subdomain is required")
    @Size(min = 3, max = 63)
    @Pattern(regexp = "^[a-z0-9][a-z0-9-]*[a-z0-9]$",
             message = "Subdomain can only contain lowercase letters, numbers, and hyphens")
    private String preferredSubdomain;

    @NotNull(message = "Subscription plan ID is required")
    private UUID subscriptionPlanId;
    
    private LocalDate subscriptionStartDate;
    private Boolean autoRenew = false;

    private String paymentImageUrl;
    
    @DecimalMin(value = "0.0")
    private BigDecimal paymentAmount;
    
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    private String paymentReferenceNumber;
    private String paymentNotes;

    public boolean hasPaymentInfo() {
        return paymentAmount != null && paymentAmount.compareTo(BigDecimal.ZERO) >= 0;
    }

    public boolean isPaymentInfoComplete() {
        return hasPaymentInfo() && paymentMethod != null;
    }
}
