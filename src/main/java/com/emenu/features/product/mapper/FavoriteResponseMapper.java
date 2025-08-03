package com.emenu.features.product.mapper;

import com.emenu.features.product.dto.response.FavoriteRemoveAllResponse;
import com.emenu.features.product.dto.response.FavoriteToggleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FavoriteResponseMapper {

    @Mapping(target = "timestamp", expression = "java(java.time.LocalDateTime.now())")
    FavoriteToggleResponse toToggleResponse(UUID productId, UUID userId, String action,
                                            Boolean isFavorited, String message);

    @Mapping(target = "timestamp", expression = "java(java.time.LocalDateTime.now())")
    FavoriteRemoveAllResponse toRemoveAllResponse(UUID userId, Integer removedCount, String message);

    // Helper methods for easier mapping
    default FavoriteToggleResponse createToggleResponse(UUID productId, UUID userId, 
                                                       boolean isFavorited, String action) {
        String message = switch (action) {
            case "added" -> "Product added to favorites";
            case "removed" -> "Product removed from favorites";
            case "unchanged" -> isFavorited ? "Product is already in favorites" : "Product is not in favorites";
            default -> "Favorite status updated";
        };
        
        return toToggleResponse(productId, userId, action, isFavorited, message);
    }

    default FavoriteRemoveAllResponse createRemoveAllResponse(UUID userId, int removedCount) {
        String message = String.format("Removed %d products from favorites", removedCount);
        return toRemoveAllResponse(userId, removedCount, message);
    }
}