package com.emenu.features.product.mapper;

import com.emenu.enums.product.ImageType;
import com.emenu.features.product.dto.request.ProductImageRequest;
import com.emenu.features.product.dto.response.ProductImageResponse;
import com.emenu.features.product.models.ProductImage;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ProductImageMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(source = "imageType", target = "imageType", qualifiedByName = "stringToImageType")
    public abstract ProductImage toEntity(ProductImageRequest request);

    @Mapping(source = "imageType", target = "imageType", qualifiedByName = "imageTypeToString")
    public abstract ProductImageResponse toResponse(ProductImage productImage);

    public abstract List<ProductImageResponse> toResponseList(List<ProductImage> productImages);

    @Named("stringToImageType")
    protected ImageType stringToImageType(String imageType) {
        if (imageType == null || imageType.trim().isEmpty()) return ImageType.GALLERY;
        try {
            return ImageType.valueOf(imageType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ImageType.GALLERY;
        }
    }

    @Named("imageTypeToString")
    protected String imageTypeToString(ImageType imageType) {
        return imageType != null ? imageType.name() : "GALLERY";
    }
}