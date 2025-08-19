package com.emenu.features.product.dto.response;

import com.emenu.enums.product.ProductStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ProductListDto {
    private UUID id;
    private String name;
    private ProductStatus status;
    
    // Original fields from product table (always from database)
    private BigDecimal price;
    private String promotionType;
    private BigDecimal promotionValue;
    private LocalDateTime promotionFromDate;
    private LocalDateTime promotionToDate;
    
    // NEW: Display fields (conditional logic based on sizes)
    private BigDecimal displayOriginPrice;
    private String displayPromotionType;
    private BigDecimal displayPromotionValue;
    private LocalDateTime displayPromotionFromDate;
    private LocalDateTime displayPromotionToDate;
    
    // Calculated fields
    private BigDecimal displayPrice;
    private Boolean hasPromotion;
    private Boolean hasSizes;
    private String mainImageUrl;
    private Long viewCount;
    private Long favoriteCount;
    private Boolean isFavorited;
    private LocalDateTime createdAt;
    
    // Business info
    private UUID businessId;
    private String businessName;
    
    // Category info
    private UUID categoryId;
    private String categoryName;
    
    // Brand info
    private UUID brandId;
    private String brandName;
} 