package com.emenu.features.order.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CartItemResponse {
    private UUID id;

    // Product info grouped for easy identification
    private CartItemProductInfo product;

    // Current pricing (always real-time from product)
    private BigDecimal currentPrice;      // Current base price
    private BigDecimal finalPrice;        // Current price with active promotions
    private Boolean hasActivePromotion;   // Whether current price has active promotion

    private Integer quantity;
    private BigDecimal totalPrice;        // finalPrice * quantity

    // Promotion details (for display)
    private String promotionType;         // PERCENTAGE or FIXED_AMOUNT
    private BigDecimal promotionValue;
    private LocalDateTime promotionFromDate;
    private LocalDateTime promotionToDate;
}