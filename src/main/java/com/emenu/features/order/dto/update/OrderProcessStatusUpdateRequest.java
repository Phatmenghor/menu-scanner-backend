package com.emenu.features.order.dto.update;

import com.emenu.enums.common.Status;
import lombok.Data;

@Data
public class OrderProcessStatusUpdateRequest {
    private String name;
    private String description;
    private String color;
    private Integer sortOrder;
    private Boolean isDefault;
    private Boolean isFinal;
    private String statusType;
    private Status status;
}
