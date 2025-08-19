package com.emenu.features.product.dto.update;

import com.emenu.enums.product.ProductStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class ProductUpdateDto {
    
    private String name;
    private String description;
    private UUID categoryId;
    private UUID brandId;
    
    @DecimalMin(value = "0.0", message = "Price must be non-negative")
    private BigDecimal price;
    
    // Promotion fields (optional)
    private String promotionType;
    private BigDecimal promotionValue;
    private LocalDateTime promotionFromDate;
    private LocalDateTime promotionToDate;
    
    // Collections (optional) - for updates, these will replace existing
    @Valid
    private List<ProductImageUpdateDto> images;
    
    @Valid
    private List<ProductSizeUpdateDto> sizes;
    
    private ProductStatus status = ProductStatus.ACTIVE;
    
    // Helper method to check if promotion data is provided
    public boolean hasPromotionData() {
        return promotionType != null && promotionValue != null;
    }
    
    // Helper method to clear promotion data
    public void clearPromotion() {
        this.promotionType = null;
        this.promotionValue = null;
        this.promotionFromDate = null;
        this.promotionToDate = null;
    }
}