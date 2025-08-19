package com.emenu.features.product.mapper;

import com.emenu.enums.product.ImageType;
import com.emenu.features.product.dto.request.ProductImageCreateDto;
import com.emenu.features.product.dto.response.ProductImageDto;
import com.emenu.features.product.dto.update.ProductImageUpdateDto;
import com.emenu.features.product.models.ProductImage;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductImageMapper {

    // ================================
    // CREATE OPERATIONS
    // ================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productId", ignore = true)
    @Mapping(source = "imageType", target = "imageType", qualifiedByName = "imageStringToImageType")
    @Mapping(target = "product", ignore = true)
    ProductImage toEntity(ProductImageCreateDto dto);

    // ================================
    // UPDATE OPERATIONS
    // ================================

    @Mapping(target = "productId", ignore = true) // Keep existing
    @Mapping(source = "imageType", target = "imageType", qualifiedByName = "imageStringToImageType")
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true) // Keep original
    @Mapping(target = "updatedAt", ignore = true) // Auto-updated
    @Mapping(target = "createdBy", ignore = true) // Keep original
    @Mapping(target = "updatedBy", ignore = true) // Auto-updated
    @Mapping(target = "isDeleted", ignore = true) // Keep existing
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateEntityFromDto(ProductImageUpdateDto dto, @MappingTarget ProductImage entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productId", ignore = true)
    @Mapping(source = "imageType", target = "imageType", qualifiedByName = "imageStringToImageType")
    @Mapping(target = "product", ignore = true)
    ProductImage toEntityFromUpdate(ProductImageUpdateDto dto);

    // ================================
    // RESPONSE MAPPING
    // ================================

    @Mapping(source = "imageType", target = "imageType", qualifiedByName = "imageTypeToString")
    ProductImageDto toDto(ProductImage entity);

    List<ProductImageDto> toDtos(List<ProductImage> entities);

    // ================================
    // BATCH OPERATIONS FOR UPDATES
    // ================================

    /**
     * Convert update DTOs to entities for new images
     */
    default List<ProductImage> toEntitiesFromUpdate(List<ProductImageUpdateDto> dtos) {
        if (dtos == null) {
            return null;
        }
        
        return dtos.stream()
                .filter(dto -> !dto.shouldDelete() && dto.isNew())
                .map(this::toEntityFromUpdate)
                .toList();
    }

    /**
     * Get IDs of images to delete
     */
    default List<java.util.UUID> getIdsToDelete(List<ProductImageUpdateDto> dtos) {
        if (dtos == null) {
            return List.of();
        }
        
        return dtos.stream()
                .filter(dto -> dto.shouldDelete() && dto.isExisting())
                .map(ProductImageUpdateDto::getId)
                .toList();
    }

    /**
     * Get DTOs for existing images to update
     */
    default List<ProductImageUpdateDto> getExistingToUpdate(List<ProductImageUpdateDto> dtos) {
        if (dtos == null) {
            return List.of();
        }
        
        return dtos.stream()
                .filter(dto -> !dto.shouldDelete() && dto.isExisting())
                .toList();
    }

    // ================================
    // CONVERSION HELPERS - UNIQUE METHOD NAMES
    // ================================

    @Named("imageStringToImageType")
    default ImageType imageStringToImageType(String imageType) {
        if (imageType == null || imageType.trim().isEmpty()) {
            return ImageType.GALLERY;
        }
        try {
            return ImageType.valueOf(imageType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ImageType.GALLERY;
        }
    }

    @Named("imageTypeToString")
    default String imageTypeToString(ImageType imageType) {
        return imageType != null ? imageType.name() : ImageType.GALLERY.toString();
    }
}