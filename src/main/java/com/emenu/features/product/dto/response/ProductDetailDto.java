package com.emenu.features.product.dto.response;

import com.emenu.enums.product.ProductStatus;
import com.emenu.shared.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProductDetailDto extends BaseAuditResponse {
    private UUID id;
    private String name;
    private String description;
    private ProductStatus status;

    private BigDecimal price;
    private String promotionType;
    private BigDecimal promotionValue;
    private LocalDateTime promotionFromDate;
    private LocalDateTime promotionToDate;

    private BigDecimal displayOriginPrice;
    private String displayPromotionType;
    private BigDecimal displayPromotionValue;
    private LocalDateTime displayPromotionFromDate;
    private LocalDateTime displayPromotionToDate;

    private BigDecimal displayPrice;
    private Boolean hasPromotion;
    private Boolean hasSizes;
    private String mainImageUrl;
    private Long viewCount;
    private Long favoriteCount;
    private Boolean isFavorited;

    private UUID businessId;
    private String businessName;

    private UUID categoryId;
    private String categoryName;

    private UUID brandId;
    private String brandName;

    private List<ProductImageDto> images;
    private List<ProductSizeDto> sizes;
}