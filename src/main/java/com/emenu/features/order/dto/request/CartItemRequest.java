package com.emenu.features.order.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CartItemRequest {
    
    @NotNull(message = "Product ID is required")
    private UUID productId;
    
    // Optional - only required for products with sizes
    private UUID productSizeId;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}