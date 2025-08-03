package com.emenu.features.product.mapper;

import com.emenu.enums.product.PromotionType;
import com.emenu.features.product.dto.request.ProductSizeRequest;
import com.emenu.features.product.dto.response.ProductSizeResponse;
import com.emenu.features.product.models.ProductSize;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ProductSizeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(source = "promotionType", target = "promotionType", qualifiedByName = "stringToPromotionType")
    public abstract ProductSize toEntity(ProductSizeRequest request);

    @Mapping(source = "promotionType", target = "promotionType", qualifiedByName = "promotionTypeToString")
    @Mapping(target = "finalPrice", expression = "java(productSize.getFinalPrice())")
    @Mapping(target = "isPromotionActive", expression = "java(productSize.isPromotionActive())")
    public abstract ProductSizeResponse toResponse(ProductSize productSize);

    public abstract List<ProductSizeResponse> toResponseList(List<ProductSize> productSizes);

    @Named("stringToPromotionType")
    protected PromotionType stringToPromotionType(String promotionType) {
        if (promotionType == null || promotionType.trim().isEmpty()) return null;
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