package com.emenu.features.order.dto.response;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusHistoryResponse {
    private UUID id;
    private String statusName;
    private String statusDescription;
    private String note;

    // User who changed the status - nested object with full details
    @Valid
    private OrderStatusHistoryUserInfo changedBy;

    private LocalDateTime changedAt;
}
