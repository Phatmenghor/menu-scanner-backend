package com.emenu.features.product.dto.filter;

import com.emenu.enums.product.ProductStatus;
import com.emenu.shared.dto.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProductFilterRequest extends BaseFilterRequest {
    private UUID businessId;
    private UUID categoryId;
    private UUID brandId;
    private ProductStatus status;
    private Boolean hasPromotion;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}