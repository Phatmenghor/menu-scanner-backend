package com.emenu.features.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BusinessOwnerCreateRequest {
    
    // ✅ BUSINESS INFORMATION
    @NotBlank(message = "Business name is required")
    @Size(max = 100, message = "Business name cannot exceed 100 characters")
    private String businessName;

    @Email(message = "Business email format is invalid")
    private String businessEmail;

    private String businessPhone;
    private String businessAddress;
    
    @Size(max = 1000, message = "Business description cannot exceed 1000 characters")
    private String businessDescription;

    // ✅ SUBDOMAIN INFORMATION
    @NotBlank(message = "Preferred subdomain is required")
    @Size(min = 3, max = 63, message = "Subdomain must be between 3 and 63 characters")
    private String preferredSubdomain;
    
    // ✅ OWNER INFORMATION
    @NotBlank(message = "Owner email is required")
    @Email(message = "Owner email format is invalid")
    private String ownerEmail;

    @NotBlank(message = "Owner password is required")
    @Size(min = 8, message = "Owner password must be at least 8 characters")
    private String ownerPassword;

    @NotBlank(message = "Owner first name is required")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String ownerFirstName;

    @NotBlank(message = "Owner last name is required")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String ownerLastName;

    private String ownerPhone;
    private String ownerAddress;
}