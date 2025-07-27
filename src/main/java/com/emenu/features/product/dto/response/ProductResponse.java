package com.emenu.features.product.dto.response;

import com.emenu.enums.product.ProductStatus;
import com.emenu.shared.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProductResponse extends BaseAuditResponse {
    private UUID businessId;
    private String businessName;
    private UUID categoryId;
    private String categoryName;
    private UUID brandId;
    private String brandName;
    
    private String name;
    private String description;
    private ProductStatus status;
    
    // Images
    private List<ProductImageResponse> images;
    private String mainImageUrl; // Main image for listing
    
    // Sizes and Pricing
    private List<ProductSizeResponse> sizes;
    private BigDecimal startingPrice; // Lowest price among all sizes
    private BigDecimal displayPrice; // Price to show on listing (after promotion)
    private Boolean hasPromotion;
    private Boolean hasMultipleSizes;
    
    // Public URL for customers
    private String publicUrl;
    
    // Statistics
    private Long favoriteCount;
    private Long viewCount;
    private Boolean isFavorited; // For current user (if logged in)
}