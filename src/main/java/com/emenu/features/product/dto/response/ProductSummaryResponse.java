package com.emenu.features.product.dto.response;

import com.emenu.enums.product.ProductStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ProductSummaryResponse {
    private UUID id;
    private String name;
    private String description;
    private ProductStatus status;
    private String mainImageUrl;
    private String categoryName;
    private String brandName;
    private BigDecimal displayPrice;
    private Boolean hasPromotion;
    private Boolean hasMultipleSizes;
    private String publicUrl;
    private Long favoriteCount;
    private Boolean isFavorited;
}