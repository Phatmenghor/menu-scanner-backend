package com.emenu.features.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPromotionResetResponse {
    private UUID productId;
    private String productName;
    private UUID businessId;
    private Boolean productHadPromotion;
    private Integer sizesWithPromotions;
    private Integer totalPromotionsReset;
    private LocalDateTime timestamp;
    private String message;
}