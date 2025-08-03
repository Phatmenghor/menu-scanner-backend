package com.emenu.features.order.dto.request;

import com.emenu.enums.common.Status;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DeliveryOptionCreateRequest {
    
    @NotBlank(message = "Delivery name is required")
    private String name;
    
    private String description;
    private String imageUrl;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", message = "Price must be non-negative")
    private BigDecimal price;
    
    private Status status = Status.ACTIVE;
}