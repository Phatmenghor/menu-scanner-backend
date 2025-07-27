package com.emenu.features.product.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ProductSizeResponse {
    private UUID id;
    private String name;
    private BigDecimal price;
    private Boolean hasPromotion;
    private String promotionType;
    private BigDecimal promotionValue;
    private BigDecimal finalPrice;
    private Boolean isDefault;
    private Integer sortOrder;
}