package com.emenu.features.product.dto.request;

import com.emenu.enums.product.ProductStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    
    // Price for products without sizes
    @DecimalMin(value = "0.0", message = "Price must be non-negative")
    private BigDecimal price;
    
    // Promotion fields for products without sizes
    private String promotionType; // PERCENTAGE or FIXED_AMOUNT
    private BigDecimal promotionValue;
    private LocalDateTime promotionFromDate;
    private LocalDateTime promotionToDate;
    
    // Images
    @NotEmpty(message = "At least one image is required")
    @Valid
    private List<ProductImageRequest> images;
    
    // Sizes and Pricing (optional - if provided, product-level price is ignored)
    @Valid
    private List<ProductSizeRequest> sizes;
    
    private ProductStatus status = ProductStatus.ACTIVE;
}