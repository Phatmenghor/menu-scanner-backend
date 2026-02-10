package com.emenu.features.order.dto.update;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderStatusUpdateRequest {

    @NotBlank(message = "Order process status is required")
    private String orderProcessStatusName;

    private String businessNote;
}
