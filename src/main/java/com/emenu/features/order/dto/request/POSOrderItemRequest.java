package com.emenu.features.order.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class POSOrderItemRequest {
    
    @NotNull(message = "Product ID is required")
    private UUID productId;
    
    private UUID productSizeId; // Optional for products with sizes
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}