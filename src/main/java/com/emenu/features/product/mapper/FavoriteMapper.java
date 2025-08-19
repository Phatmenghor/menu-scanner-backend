package com.emenu.features.product.mapper;

import com.emenu.features.product.dto.response.FavoriteToggleDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface FavoriteMapper {

    @Mapping(target = "timestamp", expression = "java(java.time.LocalDateTime.now())")
    FavoriteToggleDto toToggleDto(UUID productId, UUID userId, String action,
                                  Boolean isFavorited, String message);

    default FavoriteToggleDto createToggleResponse(UUID productId, UUID userId,
                                                   boolean isFavorited, String action) {
        String message = switch (action) {
            case "added" -> "Product added to favorites";
            case "removed" -> "Product removed from favorites";
            case "unchanged" -> isFavorited ? "Product is already in favorites" : "Product is not in favorites";
            default -> "Favorite status updated";
        };
        
        return toToggleDto(productId, userId, action, isFavorited, message);
    }
}