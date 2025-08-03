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
public class SizePromotionResetResponse {
    private UUID productId;
    private UUID sizeId;
    private String productName;
    private String sizeName;
    private UUID businessId;
    private Boolean hadPromotion;
    private String originalPromotionType;
    private LocalDateTime timestamp;
    private String message;
}