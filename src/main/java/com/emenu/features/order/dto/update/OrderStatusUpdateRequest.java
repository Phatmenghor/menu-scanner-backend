package com.emenu.features.order.dto.update;

import com.emenu.enums.order.OrderStatus;
import lombok.Data;

@Data
public class OrderStatusUpdateRequest {
    private OrderStatus status;
    private String businessNote;
}
