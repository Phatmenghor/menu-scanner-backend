package com.emenu.features.order.dto.helper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Helper DTO for creating new Cart via MapStruct
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartCreateHelper {
    private UUID userId;
    private UUID businessId;
}
