package com.emenu.features.order.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DeliveryOptionRequest {
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal price;
}
