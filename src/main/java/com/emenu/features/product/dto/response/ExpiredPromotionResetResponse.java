package com.emenu.features.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpiredPromotionResetResponse {
    private Integer productPromotionsReset;
    private Integer sizePromotionsReset;
    private Integer totalReset;
    private LocalDateTime timestamp;
    private String message;
}
