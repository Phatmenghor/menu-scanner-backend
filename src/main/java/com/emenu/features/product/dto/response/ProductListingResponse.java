package com.emenu.features.product.dto.response;

import com.emenu.enums.product.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Lightweight response DTO for product listings
 * Optimized for performance - no collections, calculated fields included
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListingResponse {
    private UUID id;
    private UUID businessId;
    private String businessName;
    private UUID categoryId;
    private String categoryName;
    private UUID brandId;
    private String brandName;
    
    private String name;
    private BigDecimal price;
    private String description;
    private ProductStatus status;
    
    // Product-level promotion fields (for products without sizes)
    private String promotionType;
    private BigDecimal promotionValue;
    
    // Pricing - optimized calculation from database
    private BigDecimal displayPrice; // Calculated lowest price (with promotions)
    private Boolean hasActivePromotion; // Calculated from database
    private Boolean hasSizes; // Calculated from database
    
    // Single main image only for listing
    private String mainImageUrl; // Pre-fetched main image URL
    
    // Statistics
    private Long favoriteCount;
    private Long viewCount;
    private Boolean isFavorited; // Set separately for authenticated users
    
    // Public URL for customers
    private String publicUrl;
    
    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}