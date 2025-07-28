package com.emenu.features.product.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductSizeRequest {
    
    @NotBlank(message = "Size name is required")
    private String name; // Small, Medium, Large, etc.
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", message = "Price must be non-negative")
    private BigDecimal price;
    
    // Promotion fields
    private String promotionType; // PERCENTAGE or FIXED_AMOUNT
    private BigDecimal promotionValue;
    private LocalDateTime promotionFromDate;
    private LocalDateTime promotionToDate;
}