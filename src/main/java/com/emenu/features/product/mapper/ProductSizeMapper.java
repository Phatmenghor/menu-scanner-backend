package com.emenu.features.product.mapper;

import com.emenu.features.product.dto.request.ProductSizeCreateDto;
import com.emenu.features.product.dto.response.ProductSizeDto;
import com.emenu.features.product.models.ProductSize;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductSizeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productId", ignore = true)
    @Mapping(source = "promotionType", target = "promotionType", qualifiedByName = "sizeStringToPromotionType")
    ProductSize toEntity(ProductSizeCreateDto dto);

    @Mapping(target = "finalPrice", expression = "java(entity.getFinalPrice())")
    @Mapping(target = "hasPromotion", expression = "java(entity.isPromotionActive())")
    @Mapping(source = "promotionType", target = "promotionType", qualifiedByName = "sizePromotionTypeToString")
    ProductSizeDto toDto(ProductSize entity);

    List<ProductSizeDto> toDtos(List<ProductSize> entities);

    @Named("sizeStringToPromotionType")
    default com.emenu.enums.product.PromotionType sizeStringToPromotionType(String promotionType) {
        if (promotionType == null || promotionType.trim().isEmpty()) {
            return null;
        }
        try {
            return com.emenu.enums.product.PromotionType.valueOf(promotionType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Named("sizePromotionTypeToString")
    default String sizePromotionTypeToString(com.emenu.enums.product.PromotionType promotionType) {
        return promotionType != null ? promotionType.name() : null;
    }
}
