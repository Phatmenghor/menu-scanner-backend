package com.emenu.features.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BusinessOwnerCreateRequest {
    
    // ✅ BUSINESS INFORMATION
    @NotBlank(message = "Business name is required")
    private String businessName;

    @Email(message = "Business email format is invalid")
    private String businessEmail;

    @Pattern(regexp = "^(\\+855|0)?[1-9][0-9]{7,8}$", message = "Invalid phone number format for Cambodia")
    private String businessPhone;
    
    private String businessAddress;
    private String businessDescription;

    // ✅ SUBDOMAIN INFORMATION
    @NotBlank(message = "Preferred subdomain is required")
    @Size(min = 3, max = 63, message = "Subdomain must be between 3 and 63 characters")
    @Pattern(regexp = "^[a-z0-9][a-z0-9-]*[a-z0-9]$", 
             message = "Subdomain can only contain lowercase letters, numbers, and hyphens. Cannot start or end with hyphen")
    private String preferredSubdomain;
    
    // ✅ OWNER INFORMATION
    @NotBlank(message = "Owner email is required")
    @Email(message = "Owner email format is invalid")
    private String ownerEmail;

    @NotBlank(message = "Owner password is required")
    @Size(min = 4, max = 100, message = "Owner password must be between 8 and 100 characters")
    private String ownerPassword;

    @NotBlank(message = "Owner first name is required")
    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    private String ownerFirstName;

    @NotBlank(message = "Owner last name is required") 
    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    private String ownerLastName;

    @Pattern(regexp = "^(\\+855|0)?[1-9][0-9]{7,8}$", message = "Invalid phone number format for Cambodia")
    private String ownerPhone;
    private String ownerAddress;
}