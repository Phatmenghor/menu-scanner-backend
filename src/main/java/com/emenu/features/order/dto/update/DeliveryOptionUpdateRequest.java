package com.emenu.features.order.dto.update;

import com.emenu.enums.common.Status;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DeliveryOptionUpdateRequest {
    private String name;
    private String description;
    private String imageUrl;
    
    @DecimalMin(value = "0.0", message = "Price must be non-negative")
    private BigDecimal price;
    
    private Status status;
}