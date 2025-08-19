package com.emenu.features.product.mapper;

import com.emenu.enums.product.PromotionType;
import com.emenu.features.product.dto.request.ProductSizeCreateDto;
import com.emenu.features.product.dto.response.ProductSizeDto;
import com.emenu.features.product.dto.update.ProductSizeUpdateDto;
import com.emenu.features.product.models.ProductSize;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductSizeMapper {

    // ================================
    // CREATE OPERATIONS
    // ================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productId", ignore = true)
    @Mapping(source = "promotionType", target = "promotionType", qualifiedByName = "sizeStringToPromotionType")
    @Mapping(target = "product", ignore = true)
    ProductSize toEntity(ProductSizeCreateDto dto);

    // ================================
    // UPDATE OPERATIONS
    // ================================

    @Mapping(target = "productId", ignore = true) // Keep existing
    @Mapping(source = "promotionType", target = "promotionType", qualifiedByName = "sizeStringToPromotionType")
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true) // Keep original
    @Mapping(target = "updatedAt", ignore = true) // Auto-updated
    @Mapping(target = "createdBy", ignore = true) // Keep original
    @Mapping(target = "updatedBy", ignore = true) // Auto-updated
    @Mapping(target = "isDeleted", ignore = true) // Keep existing
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateEntityFromDto(ProductSizeUpdateDto dto, @MappingTarget ProductSize entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productId", ignore = true)
    @Mapping(source = "promotionType", target = "promotionType", qualifiedByName = "sizeStringToPromotionType")
    @Mapping(target = "product", ignore = true)
    ProductSize toEntityFromUpdate(ProductSizeUpdateDto dto);

    // ================================
    // RESPONSE MAPPING
    // ================================

    @Mapping(target = "finalPrice", expression = "java(entity.getFinalPrice())")
    @Mapping(target = "hasPromotion", expression = "java(entity.isPromotionActive())")
    @Mapping(source = "promotionType", target = "promotionType", qualifiedByName = "sizePromotionTypeToString")
    ProductSizeDto toDto(ProductSize entity);

    List<ProductSizeDto> toDtos(List<ProductSize> entities);

    // ================================
    // BATCH OPERATIONS FOR UPDATES
    // ================================

    /**
     * Convert update DTOs to entities for new sizes
     */
    default List<ProductSize> toEntitiesFromUpdate(List<ProductSizeUpdateDto> dtos) {
        if (dtos == null) {
            return null;
        }
        
        return dtos.stream()
                .filter(dto -> !dto.shouldDelete() && dto.isNew())
                .map(dto -> {
                    ProductSize size = toEntityFromUpdate(dto);
                    if (!dto.hasPromotionData()) {
                        size.removePromotion();
                    }
                    return size;
                })
                .toList();
    }

    /**
     * Get IDs of sizes to delete
     */
    default List<java.util.UUID> getIdsToDelete(List<ProductSizeUpdateDto> dtos) {
        if (dtos == null) {
            return List.of();
        }
        
        return dtos.stream()
                .filter(dto -> dto.shouldDelete() && dto.isExisting())
                .map(ProductSizeUpdateDto::getId)
                .toList();
    }

    /**
     * Get DTOs for existing sizes to update
     */
    default List<ProductSizeUpdateDto> getExistingToUpdate(List<ProductSizeUpdateDto> dtos) {
        if (dtos == null) {
            return List.of();
        }
        
        return dtos.stream()
                .filter(dto -> !dto.shouldDelete() && dto.isExisting())
                .toList();
    }

    // ================================
    // PROMOTION HANDLING
    // ================================

    @AfterMapping
    default void afterSizeUpdateMapping(ProductSizeUpdateDto dto, @MappingTarget ProductSize entity) {
        // Handle promotion clearing
        if (!dto.hasPromotionData()) {
            entity.removePromotion();
        }
    }

    // ================================
    // CONVERSION HELPERS - UNIQUE METHOD NAMES
    // ================================

    @Named("sizeStringToPromotionType")
    default PromotionType sizeStringToPromotionType(String promotionType) {
        if (promotionType == null || promotionType.trim().isEmpty()) {
            return null;
        }
        try {
            return PromotionType.valueOf(promotionType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Named("sizePromotionTypeToString")
    default String sizePromotionTypeToString(PromotionType promotionType) {
        return promotionType != null ? promotionType.name() : null;
    }
}