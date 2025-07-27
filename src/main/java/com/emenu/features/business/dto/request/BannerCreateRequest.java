package com.emenu.features.business.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class BannerCreateRequest {
    
    @NotNull(message = "Business ID is required")
    private UUID businessId;

    private String imageUrl;
    
    private String linkUrl;
    private Boolean isActive = true;
}
