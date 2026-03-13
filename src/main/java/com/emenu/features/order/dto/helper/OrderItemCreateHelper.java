package com.emenu.features.order.dto.helper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Helper DTO for creating OrderItem via MapStruct
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemCreateHelper {
    private UUID orderId;
    private UUID productId;
    private UUID productSizeId;
    private String productName;
    private String productImageUrl;
    private String sizeName;

    // Pricing snapshot
    private BigDecimal currentPrice;  // Base price before discount
    private BigDecimal finalPrice;    // Price after discount
    private BigDecimal unitPrice;     // Same as finalPrice for backward compat
    private Boolean hasPromotion;

    // Promotion details
    private String promotionType;
    private BigDecimal promotionValue;
    private LocalDateTime promotionFromDate;
    private LocalDateTime promotionToDate;

    private Integer quantity;

    // Customer instructions
    private String specialInstructions;
}
