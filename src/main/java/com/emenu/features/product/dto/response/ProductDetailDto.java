package com.emenu.features.product.dto.response;

import com.emenu.enums.product.ProductStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ProductDetailDto {
    private UUID id;
    private String name;
    private String description;
    private ProductStatus status;
    private BigDecimal price;
    private BigDecimal displayPrice;
    private Boolean hasPromotion;
    private Boolean hasSizes;
    private Long viewCount;
    private Long favoriteCount;
    private Boolean isFavorited;
    private LocalDateTime createdAt;
    
    // Promotion details
    private String promotionType;
    private BigDecimal promotionValue;
    private LocalDateTime promotionFromDate;
    private LocalDateTime promotionToDate;
    
    // Business info
    private UUID businessId;
    private String businessName;
    
    // Category info
    private UUID categoryId;
    private String categoryName;
    
    // Brand info
    private UUID brandId;
    private String brandName;
    
    // Collections (loaded only for detail view)
    private List<ProductImageDto> images;
    private List<ProductSizeDto> sizes;
}