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
public class BusinessPromotionResetResponse {
    private UUID businessId;
    private Integer productPromotionsReset;
    private Integer sizePromotionsReset;
    private Integer totalReset;
    private LocalDateTime timestamp;
    private String message;
}