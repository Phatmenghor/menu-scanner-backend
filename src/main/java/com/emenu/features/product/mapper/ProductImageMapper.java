package com.emenu.features.product.mapper;

import com.emenu.features.product.dto.request.ProductImageRequest;
import com.emenu.features.product.dto.response.ProductImageResponse;
import com.emenu.features.product.models.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

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
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "sortOrder", ignore = true) // Will be set manually
    public abstract ProductImage toEntity(ProductImageRequest request);

    public abstract ProductImageResponse toResponse(ProductImage productImage);

    public abstract List<ProductImageResponse> toResponseList(List<ProductImage> productImages);
}