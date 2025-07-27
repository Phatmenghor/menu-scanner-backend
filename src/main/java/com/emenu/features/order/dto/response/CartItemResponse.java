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
    private UUID productSizeId;
    private String sizeName;
    private BigDecimal unitPrice;
    private BigDecimal finalPrice; // After promotion
    private Boolean hasPromotion;
    private Integer quantity;
    private BigDecimal totalPrice; // finalPrice * quantity
    private String notes;
    private LocalDateTime addedAt;
}