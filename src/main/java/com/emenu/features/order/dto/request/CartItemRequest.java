package com.emenu.features.order.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Cart item request - matches CartItemResponse structure
 * Frontend sends complete cart data with all calculations and edits
 */
@Data
public class CartItemRequest {
    // Cart item ID (optional, for reference)
    private UUID id;

    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotNull(message = "Product name is required")
    private String productName;

    private String productImageUrl;

    // Size info
    private UUID productSizeId;
    private String sizeName;

    // Pricing snapshot - from frontend cart (already calculated with discounts)
    @NotNull(message = "Current price is required")
    private BigDecimal currentPrice;  // Base price before discount

    @NotNull(message = "Final price is required")
    private BigDecimal finalPrice;    // Price after discount/promotion

    private Boolean hasPromotion;

    // Promotion details snapshot (if applicable)
    private String promotionType;      // PERCENTAGE or FIXED_AMOUNT
    private BigDecimal promotionValue;
    private LocalDateTime promotionEndDate;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Total price is required")
    private BigDecimal totalPrice;     // finalPrice * quantity

    // Product availability (from frontend)
    private Boolean isAvailable;

    // Customer instructions for this specific item
    private String specialInstructions;
}
