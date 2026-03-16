package com.emenu.features.order.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class OrderItemResponse {
    private UUID id;

    // Product info grouped for easy identification
    private OrderItemProductInfo product;

    // Pricing (snapshot from order, not real-time)
    private BigDecimal currentPrice;        // Base price at time of order
    private BigDecimal finalPrice;          // Price with promotions at time of order
    private Boolean hasActivePromotion;     // Whether it had active promotion when ordered

    private Integer quantity;

    // Detailed pricing breakdown
    private BigDecimal totalBeforeDiscount; // currentPrice * quantity
    private BigDecimal discountAmount;      // totalBeforeDiscount - totalPrice (discount per item line)
    private BigDecimal totalPrice;          // finalPrice * quantity (final total after discount)

    // Promotion details (snapshot from order time)
    private String promotionType;           // PERCENTAGE or FIXED_AMOUNT
    private BigDecimal promotionValue;
    private LocalDateTime promotionFromDate;
    private LocalDateTime promotionToDate;

    @Data
    public static class OrderItemProductInfo {
        private UUID id;
        private String name;
        private String imageUrl;
        private UUID sizeId;
        private String sizeName;
        private String status;
    }
}