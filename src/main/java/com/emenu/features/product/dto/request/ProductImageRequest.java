package com.emenu.features.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductImageRequest {
    
    @NotBlank(message = "Image URL is required")
    private String imageUrl;
    
    private Boolean isMain = false; // Main image for listing
    
    private Integer sortOrder = 0; // For ordering images
}