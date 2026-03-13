package com.emenu.features.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Clean DTO for order status snapshot - no inheritance, no nulls
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusDto {
    private String name;
    private LocalDateTime createdAt;  // When this status was set
}
