package com.emenu.features.order.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CartItemResponse {
    private UUID id;
    private UUID productId;
    private String productName;
    private String productImageUrl;
    private UUID productSizeId; // Nullable for products without sizes
    private String sizeName; // "Standard" for products without sizes
    
    // Current pricing (always real-time from product)
    private BigDecimal currentPrice; // Current base price
    private BigDecimal finalPrice; // Current price with active promotions
    private Boolean hasPromotion; // If current price has active promotion
    private BigDecimal discountAmount; // Current discount amount
    
    private Integer quantity;
    private BigDecimal totalPrice; // finalPrice * quantity

    // Product availability
    private Boolean isAvailable; // Product is active and not deleted
    private Boolean isInStock; // Product is available for purchase

    // Promotion details (for display)
    private String promotionType; // PERCENTAGE or FIXED_AMOUNT
    private BigDecimal promotionValue;
    private LocalDateTime promotionEndDate;
}