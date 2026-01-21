package com.emenu.features.main.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class FavoriteToggleDto {
    private UUID productId;
    private UUID userId;
    private String action;
    private Boolean isFavorited;
    private LocalDateTime timestamp;
    private String message;
}