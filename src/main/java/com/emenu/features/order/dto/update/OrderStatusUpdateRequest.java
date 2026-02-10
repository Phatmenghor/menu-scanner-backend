package com.emenu.features.order.dto.update;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class OrderStatusUpdateRequest {

    @NotNull(message = "Order process status is required")
    private UUID orderProcessStatusId;

    private String businessNote;
}