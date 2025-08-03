package com.emenu.features.order.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class OrderCreateRequest {
    
    @NotNull(message = "Business ID is required")
    private UUID businessId;
    
    private UUID deliveryAddressId; // Optional for pickup orders
    private UUID deliveryOptionId; // Optional for pickup orders
    private String customerNote;
}