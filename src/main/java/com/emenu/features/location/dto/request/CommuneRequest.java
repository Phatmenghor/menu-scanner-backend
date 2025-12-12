package com.emenu.features.location.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommuneRequest {
    @NotBlank(message = "Commune code is required")
    private String communeCode;
    
    @NotBlank(message = "Commune name (EN) is required")
    private String communeEn;
    
    @NotBlank(message = "Commune name (KH) is required")
    private String communeKh;
    
    @NotBlank(message = "District code is required")
    private String districtCode;
}
