package com.emenu.features.product.dto.request;

import com.emenu.enums.product.ProductStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ProductCreateRequest {
    
    @NotBlank(message = "Product name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Category is required")
    private UUID categoryId;
    
    private UUID brandId; // Optional
    
    // Images
    @NotEmpty(message = "At least one image is required")
    @Valid
    private List<ProductImageRequest> images;
    
    // Sizes and Pricing
    @NotEmpty(message = "At least one size/price is required")
    @Valid
    private List<ProductSizeRequest> sizes;
    
    private ProductStatus status = ProductStatus.ACTIVE;
}