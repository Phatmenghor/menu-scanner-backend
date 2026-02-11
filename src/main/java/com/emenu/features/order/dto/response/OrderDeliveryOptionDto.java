package com.emenu.features.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Clean DTO for order delivery option snapshot - no inheritance, no nulls
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDeliveryOptionDto {
    private String name;
    private String description;
    private BigDecimal price;
}
