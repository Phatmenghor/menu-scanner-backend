package com.emenu.features.product.dto.response;

import com.emenu.enums.product.ProductStatus;
import com.emenu.shared.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProductListDto  extends BaseAuditResponse {
    private UUID id;
    private String name;
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
    private UUID categoryId;
    private UUID brandId;
}