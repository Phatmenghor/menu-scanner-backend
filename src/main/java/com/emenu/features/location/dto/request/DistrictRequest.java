package com.emenu.features.location.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DistrictRequest {
    @NotBlank(message = "District code is required")
    private String districtCode;
    
    @NotBlank(message = "District name (EN) is required")
    private String districtEn;
    
    @NotBlank(message = "District name (KH) is required")
    private String districtKh;

    @NotBlank(message = "Province code is required")
    private String provinceCode;
}
