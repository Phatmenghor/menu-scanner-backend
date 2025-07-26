package com.emenu.features.subdomain.dto.update;

import com.emenu.enums.subdomain.SubdomainStatus;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SubdomainUpdateRequest {
    
    @Size(min = 3, max = 63, message = "Subdomain must be between 3 and 63 characters")
    @Pattern(regexp = "^[a-z0-9][a-z0-9-]*[a-z0-9]$",
             message = "Subdomain can only contain lowercase letters, numbers, and hyphens. Cannot start or end with hyphen.")
    private String subdomain;
    
    private SubdomainStatus status;
    private Boolean isActive;
    private Boolean sslEnabled;
    private String customDomain;
    private String notes;
}