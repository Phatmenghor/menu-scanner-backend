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
    private BigDecimal unitPrice; // Original price when added to cart
    private BigDecimal finalPrice; // Current price with active promotions
    private Boolean hasPromotion; // If current price is different from unit price
    private Integer quantity;
    private BigDecimal totalPrice; // finalPrice * quantity
    private String notes;
    private LocalDateTime addedAt;
}