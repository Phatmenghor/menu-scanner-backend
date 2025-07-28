package com.emenu.features.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductImageRequest {
    
    @NotBlank(message = "Image URL is required")
    private String imageUrl;
    
    private String imageType = "GALLERY"; // MAIN or GALLERY
}