package com.emenu.features.order.dto.response;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Current status snapshot with full user details who set it
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusDto {
    private String name;
    private String description;

    // Who changed to this status - nested object with full details
    @Valid
    private OrderStatusHistoryUserInfo changedBy;

    private LocalDateTime createdAt;  // When this status was set
}
