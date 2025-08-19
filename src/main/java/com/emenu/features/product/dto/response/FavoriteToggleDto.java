
package com.emenu.features.product.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class FavoriteToggleDto {
    private UUID productId;
    private UUID userId;
    private String action; // "added", "removed", "unchanged"
    private Boolean isFavorited;
    private LocalDateTime timestamp;
    private String message;
}