package com.emenu.features.product.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ProductSizeDto {
    private UUID id;
    private String name;
    private BigDecimal price;
    private String promotionType;
    private BigDecimal promotionValue;
    private LocalDateTime promotionFromDate;
    private LocalDateTime promotionToDate;
    private BigDecimal finalPrice;
    private Boolean hasPromotion;
    private LocalDateTime createdAt;
}