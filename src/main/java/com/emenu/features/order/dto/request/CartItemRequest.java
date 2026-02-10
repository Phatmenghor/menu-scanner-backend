package com.emenu.features.order.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CartItemRequest {

    @NotNull(message = "Product ID is required")
    private UUID productId;

    // Optional - only required for products with sizes
    private UUID productSizeId;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be at least 0")
    private Integer quantity;

    // Full product snapshot - for checkout to preserve historical data
    private String productName;
    private String productDescription;
    private String productImageUrl;
    private BigDecimal unitPrice;
    private String sizeName;
    private String specialInstructions;
}