package com.emenu.features.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Clean DTO for order delivery option snapshot - mirrors DeliveryOptionRequest exactly
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDeliveryOptionDto {
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal price;
}
