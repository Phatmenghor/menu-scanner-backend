package com.emenu.features.product.dto.response;

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
public class FavoriteToggleResponse {
    private UUID productId;
    private UUID userId;
    private String action; // "added", "removed", "unchanged"
    private Boolean isFavorited;
    private LocalDateTime timestamp;
    private String message;
}
