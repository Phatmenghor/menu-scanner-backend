package com.emenu.features.location.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VillageRequest {
    @NotBlank(message = "Village code is required")
    private String villageCode;
    
    @NotBlank(message = "Village name (EN) is required")
    private String villageEn;
    
    @NotBlank(message = "Village name (KH) is required")
    private String villageKh;

    @NotBlank(message = "Commune code is required")
    private String communeCode;
}