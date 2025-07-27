package com.emenu.features.product.mapper;

import com.emenu.enums.product.PromotionType;
import com.emenu.features.product.dto.request.ProductSizeRequest;
import com.emenu.features.product.dto.response.ProductSizeResponse;
import com.emenu.features.product.models.ProductSize;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ProductSizeMapper {

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
    @Mapping(target = "finalPrice", ignore = true) // Will be calculated
    @Mapping(target = "sortOrder", ignore = true) // Will be set manually
    @Mapping(source = "promotionType", target = "promotionType", qualifiedByName = "stringToPromotionType")
    public abstract ProductSize toEntity(ProductSizeRequest request);

    @Mapping(source = "promotionType", target = "promotionType", qualifiedByName = "promotionTypeToString")
    public abstract ProductSizeResponse toResponse(ProductSize productSize);

    public abstract List<ProductSizeResponse> toResponseList(List<ProductSize> productSizes);

    @Named("stringToPromotionType")
    protected PromotionType stringToPromotionType(String promotionType) {
        if (promotionType == null) return null;
        try {
            return PromotionType.valueOf(promotionType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Named("promotionTypeToString")
    protected String promotionTypeToString(PromotionType promotionType) {
        return promotionType != null ? promotionType.name() : null;
    }
}