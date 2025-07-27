package com.emenu.features.product.dto.update;

import com.emenu.enums.product.ProductStatus;
import com.emenu.features.product.dto.request.ProductImageRequest;
import com.emenu.features.product.dto.request.ProductSizeRequest;
import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ProductUpdateRequest {
    private String name;
    private String description;
    private UUID categoryId;
    private UUID brandId;
    private ProductStatus status;
    
    // Images - will replace all existing images
    @Valid
    private List<ProductImageRequest> images;
    
    // Sizes - will replace all existing sizes
    @Valid
    private List<ProductSizeRequest> sizes;
}
