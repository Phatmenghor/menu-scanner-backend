package com.emenu.features.main.mapper;

import com.emenu.enums.product.PromotionType;
import com.emenu.features.main.dto.helper.ProductCreateHelper;
import com.emenu.features.main.dto.request.ProductCreateDto;
import com.emenu.features.main.dto.response.ProductDetailDto;
import com.emenu.features.main.dto.response.ProductListDto;
import com.emenu.features.main.dto.update.ProductUpdateDto;
import com.emenu.features.main.models.Product;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring",
uses = {ProductImageMapper.class, ProductSizeMapper.class, PaginationMapper.class},
unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    @Mapping(target = "viewCount", constant = "0L")
    @Mapping(target = "favoriteCount", constant = "0L")
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "sizes", ignore = true)
    @Mapping(source = "promotionType", target = "promotionType", qualifiedByName = "stringToPromotionType")
    Product toEntity(ProductCreateDto dto);

    /**
     * Apply business-specific fields to product after creation
     */
    @Mapping(target = "businessId", source = "businessId")
    @Mapping(target = "viewCount", source = "viewCount")
    @Mapping(target = "favoriteCount", source = "favoriteCount")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void applyBusinessFields(ProductCreateHelper helper, @MappingTarget Product product);

    /**
     * Helper method to set business fields on product
     */
    default Product setBusinessFields(Product product, UUID businessId) {
        ProductCreateHelper helper = ProductCreateHelper.builder()
                .businessId(businessId)
                .viewCount(0L)
                .favoriteCount(0L)
                .build();
        applyBusinessFields(helper, product);
        return product;
    }

    @AfterMapping
    default void truncateProductPromotionDates(ProductCreateDto dto, @MappingTarget Product entity) {
        entity.setPromotionFromDate(truncateToDay(entity.getPromotionFromDate()));
        entity.setPromotionToDate(truncateToDay(entity.getPromotionToDate()));
    }

    @AfterMapping
    default void afterUpdate(ProductUpdateDto dto, @MappingTarget Product entity) {
        if (!dto.hasPromotionData()) {
            entity.setPromotionType(null);
            entity.setPromotionValue(null);
            entity.setPromotionFromDate(null);
            entity.setPromotionToDate(null);
        } else {
            entity.setPromotionFromDate(truncateToDay(entity.getPromotionFromDate()));
            entity.setPromotionToDate(truncateToDay(entity.getPromotionToDate()));
        }
    }

    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "favoriteCount", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "sizes", ignore = true)
    @Mapping(source = "promotionType", target = "promotionType", qualifiedByName = "stringToPromotionType")
    void updateEntity(ProductUpdateDto dto, @MappingTarget Product entity);

    @Named("truncateToDay")
    default LocalDateTime truncateToDay(LocalDateTime dt) {
        return dt != null ? dt.truncatedTo(ChronoUnit.DAYS) : null;
    }

    @Mapping(source = "displayPromotionType", target = "displayPromotionType", qualifiedByName = "promotionTypeToString")
    @Mapping(target = "isFavorited", constant = "false")
    ProductListDto toListDto(Product product);

    List<ProductListDto> toListDtos(List<Product> products);

    @Mapping(source = "business.name", target = "businessName")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "brand.name", target = "brandName")
    @Mapping(source = "promotionType", target = "promotionType", qualifiedByName = "promotionTypeToString")
    @Mapping(source = "displayPromotionType", target = "displayPromotionType", qualifiedByName = "promotionTypeToString")
    @Mapping(target = "hasPromotion", source = "hasActivePromotion")
    @Mapping(target = "isFavorited", constant = "false")
    ProductDetailDto toDetailDto(Product product);

    @Named("stringToPromotionType")
    default PromotionType stringToPromotionType(String promotionType) {
if (promotionType == null || promotionType.trim().isEmpty()) {
    return null;
}
try {
    return PromotionType.valueOf(promotionType.toUpperCase());
} catch (IllegalArgumentException e) {
    return null;
}
    }

    @Named("promotionTypeToString")
    default String promotionTypeToString(PromotionType promotionType) {
return promotionType != null ? promotionType.name() : null;
    }

    /**
     * Convert paginated products to pagination response
     */
    default PaginationResponse<ProductListDto> toPaginationResponse(Page<Product> page, PaginationMapper paginationMapper) {
return paginationMapper.toPaginationResponse(page, this::toListDtos);
    }
}