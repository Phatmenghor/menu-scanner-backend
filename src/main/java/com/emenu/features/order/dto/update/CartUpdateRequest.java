package com.emenu.features.order.dto.update;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CartUpdateRequest {
    
    @NotNull(message = "Cart item ID is required")
    private UUID cartItemId;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be non-negative")
    private Integer quantity; // 0 means remove item
    
    private String notes;
}
