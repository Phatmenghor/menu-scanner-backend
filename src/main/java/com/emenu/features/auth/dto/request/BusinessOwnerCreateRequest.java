package com.emenu.features.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessOwnerCreateRequest {
    
    @NotBlank(message = "Owner user identifier is required")
    private String ownerUserIdentifier;
    
    @NotBlank(message = "Owner email is required")
    @Email(message = "Invalid email format")
    private String ownerEmail;
    
    @NotBlank(message = "Owner password is required")
    private String ownerPassword;
    
    @NotBlank(message = "Owner full name is required")
    private String ownerFullName;
    private String ownerPhone;
    
    @NotBlank(message = "Business name is required")
    private String businessName;
    
    @NotBlank(message = "Business email is required")
    @Email(message = "Invalid business email format")
    private String businessEmail;
    private String businessPhone;
    private String businessAddress;
    
    @NotNull(message = "Subscription plan ID is required")
    private UUID planId;
    private Integer customDurationDays;
    private BigDecimal paymentAmount;
    private String paymentMethod;
    private String paymentReference;
    private String paymentNotes;
    
    public boolean hasPaymentInfo() {
        return paymentAmount != null;
    }
    
    public boolean isPaymentInfoComplete() {
        return paymentAmount != null && paymentMethod != null;
    }
}