package com.emenu.features.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderProcessStatusCreateRequest {
    @NotBlank(message = "Status name is required")
    private String name;
    private String description;
}
