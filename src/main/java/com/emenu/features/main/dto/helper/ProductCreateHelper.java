package com.emenu.features.main.dto.helper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Helper DTO for setting business-specific fields on Product after creation via MapStruct
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateHelper {
    private UUID businessId;
    private Long viewCount;
    private Long favoriteCount;
}
