package com.emenu.features.subdomain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class SubdomainCreateRequest {
    
    @NotNull(message = "Business ID is required")
    private UUID businessId;
    
    @NotBlank(message = "Subdomain is required")
    @Size(min = 3, max = 63, message = "Subdomain must be between 3 and 63 characters")
    @Pattern(regexp = "^[a-z0-9][a-z0-9-]*[a-z0-9]$",
             message = "Subdomain can only contain lowercase letters, numbers, and hyphens. Cannot start or end with hyphen.")
    private String subdomain;
    
    private String customDomain;
    private String notes;
}