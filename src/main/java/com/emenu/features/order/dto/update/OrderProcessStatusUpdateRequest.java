package com.emenu.features.order.dto.update;

import com.emenu.enums.common.Status;
import lombok.Data;

@Data
public class OrderProcessStatusUpdateRequest {
    private String name;
    private String description;
    private Status status;
}
