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
public class FavoriteRemoveAllResponse {
    private UUID userId;
    private Integer removedCount;
    private LocalDateTime timestamp;
    private String message;
}