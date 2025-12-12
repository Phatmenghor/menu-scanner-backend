package com.emenu.features.location.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProvinceRequest {
    @NotBlank(message = "Province code is required")
    private String provinceCode;
    
    @NotBlank(message = "Province name (EN) is required")
    private String provinceEn;
    
    @NotBlank(message = "Province name (KH) is required")
    private String provinceKh;
}
