package com.emenu.features.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BusinessOwnerCreateRequest {

    // ✅ OWNER INFORMATION - UPDATED: userIdentifier required, email optional
    @NotBlank(message = "Owner user identifier is required")
    private String ownerUserIdentifier; // ✅ NEW: Required - can be anything
    
    // ✅ BUSINESS INFORMATION
    @NotBlank(message = "Business name is required")
    private String businessName;

    private String businessEmail; // ✅ UPDATED: Optional - can be null

    @Pattern(regexp = "^(\\+855|0)?[1-9][0-9]{7,8}$", message = "Invalid phone number format for Cambodia")
    private String businessPhone; // Optional - can be null
    
    private String businessAddress;
    private String businessDescription;

    // ✅ SUBDOMAIN INFORMATION
    @NotBlank(message = "Preferred subdomain is required")
    @Size(min = 3, max = 63, message = "Subdomain must be between 3 and 63 characters")
    @Pattern(regexp = "^[a-z0-9][a-z0-9-]*[a-z0-9]$", 
             message = "Subdomain can only contain lowercase letters, numbers, and hyphens. Cannot start or end with hyphen")
    private String preferredSubdomain;


    private String ownerEmail; // ✅ UPDATED: Optional - can be null

    @NotBlank(message = "Owner password is required")
    @Size(min = 4, max = 100, message = "Owner password must be between 4 and 100 characters")
    private String ownerPassword;

    @NotBlank(message = "Owner first name is required")
    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    private String ownerFirstName;

    @NotBlank(message = "Owner last name is required") 
    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    private String ownerLastName;

    @Pattern(regexp = "^(\\+855|0)?[1-9][0-9]{7,8}$", message = "Invalid phone number format for Cambodia")
    private String ownerPhone; // ✅ UPDATED: Optional - can be null
    
    private String ownerAddress;
}