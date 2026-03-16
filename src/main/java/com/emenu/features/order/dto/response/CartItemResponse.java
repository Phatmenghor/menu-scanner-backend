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
    private BigDecimal currentPrice;           // Base price
    private BigDecimal finalPrice;             // Price with active promotions
    private Boolean hasActivePromotion;        // Whether has active promotion

    private Integer quantity;

    // Detailed pricing breakdown (standardized across cart/checkout/order)
    private BigDecimal totalBeforeDiscount;    // currentPrice * quantity
    private BigDecimal discountAmount;         // totalBeforeDiscount - totalPrice (discount for this item)
    private BigDecimal totalPrice;             // finalPrice * quantity (final total after discount)

    // Promotion details (for display)
    private String promotionType;              // PERCENTAGE or FIXED_AMOUNT
    private BigDecimal promotionValue;
    private LocalDateTime promotionFromDate;
    private LocalDateTime promotionToDate;
}