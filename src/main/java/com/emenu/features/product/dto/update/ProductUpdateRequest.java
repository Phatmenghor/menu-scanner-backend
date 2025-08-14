package com.emenu.features.product.dto.update;

import com.emenu.enums.product.ProductStatus;
import com.emenu.features.product.dto.request.ProductImageRequest;
import com.emenu.features.product.dto.request.ProductSizeRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class ProductUpdateRequest {
    private String name;
    private String description;
    private UUID categoryId;
    private UUID brandId; // Optional - can be null
    private ProductStatus status;
    
    // Price for products without sizes
    @DecimalMin(value = "0.0", message = "Price must be non-negative")
    private BigDecimal price;
    
    // Promotion fields for products without sizes (all optional)
    private String promotionType; // PERCENTAGE or FIXED_AMOUNT
    private BigDecimal promotionValue;
    private LocalDateTime promotionFromDate;
    private LocalDateTime promotionToDate;
    
    // Images - will use smart CRUD logic with MAIN image handling
    @Valid
    private List<ProductImageRequest> images;
    
    // Sizes - will use CRUD logic based on IDs
    @Valid
    private List<ProductSizeRequest> sizes;
}