package com.emenu.features.product.mapper;

import com.emenu.features.product.dto.request.ProductImageCreateDto;
import com.emenu.features.product.dto.response.ProductImageDto;
import com.emenu.features.product.models.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductImageMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productId", ignore = true)
    @Mapping(source = "imageType", target = "imageType", qualifiedByName = "stringToImageType")
    ProductImage toEntity(ProductImageCreateDto dto);

    @Mapping(source = "imageType", target = "imageType", qualifiedByName = "imageTypeToString")
    ProductImageDto toDto(ProductImage entity);

    List<ProductImageDto> toDtos(List<ProductImage> entities);

    @Named("stringToImageType")
    default com.emenu.enums.product.ImageType stringToImageType(String imageType) {
        if (imageType == null || imageType.trim().isEmpty()) {
            return com.emenu.enums.product.ImageType.GALLERY;
        }
        try {
            return com.emenu.enums.product.ImageType.valueOf(imageType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return com.emenu.enums.product.ImageType.GALLERY;
        }
    }

    @Named("imageTypeToString")
    default String imageTypeToString(com.emenu.enums.product.ImageType imageType) {
        return imageType != null ? imageType.name() : "GALLERY";
    }
}
