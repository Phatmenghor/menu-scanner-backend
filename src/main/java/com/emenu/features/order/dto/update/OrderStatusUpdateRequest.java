package com.emenu.features.order.dto.update;

import com.emenu.enums.order.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderStatusUpdateRequest {
    
    @NotNull(message = "Order status is required")
    private OrderStatus status;
    
    private String businessNote;
}