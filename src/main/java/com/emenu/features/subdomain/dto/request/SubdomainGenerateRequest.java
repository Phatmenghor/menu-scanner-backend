package com.emenu.features.subdomain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SubdomainGenerateRequest {
    
    @NotBlank(message = "Business name is required")
    @Size(min = 2, max = 100, message = "Business name must be between 2 and 100 characters")
    private String businessName;
    
    // Optional: How many suggestions to generate (default 5)
    private Integer suggestionCount = 5;
}