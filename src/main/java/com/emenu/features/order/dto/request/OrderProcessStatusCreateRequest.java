package com.emenu.features.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderProcessStatusCreateRequest {

    @NotBlank(message = "Status name is required")
    private String name;

    private String description;

    private String color;

    @NotNull(message = "Sort order is required")
    private Integer sortOrder;

    private Boolean isDefault = false;

    private Boolean isFinal = false;

    @NotBlank(message = "Status type is required (ACTIVE, COMPLETED, CANCELLED)")
    private String statusType;
}
