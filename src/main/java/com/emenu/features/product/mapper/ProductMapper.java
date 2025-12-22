package com.emenu.features.product.mapper;

import com.emenu.enums.product.PromotionType;
import com.emenu.features.product.dto.request.ProductCreateDto;
import com.emenu.features.product.dto.response.ProductDetailDto;
import com.emenu.features.product.dto.response.ProductListDto;
import com.emenu.features.product.dto.update.ProductUpdateDto;
import com.emenu.features.product.models.Product;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring",
        uses = {ProductImageMapper.class, ProductSizeMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "viewCount", constant = "0L")
    @Mapping(target = "favoriteCount", constant = "0L")
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "sizes", ignore = true)
    @Mapping(source = "promotionType", target = "promotionType", qualifiedByName = "productStringToPromotionType")
    Product toEntity(ProductCreateDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "favoriteCount", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "sizes", ignore = true)
    @Mapping(source = "promotionType", target = "promotionType", qualifiedByName = "productStringToPromotionType")
    @AfterMapping
    default void afterUpdate(ProductUpdateDto dto, @MappingTarget Product entity) {
        if (!dto.hasPromotionData()) {
            entity.setPromotionType(null);
            entity.setPromotionValue(null);
            entity.setPromotionFromDate(null);
            entity.setPromotionToDate(null);
        }
    }
    void updateEntity(ProductUpdateDto dto, @MappingTarget Product entity);

    @Mapping(source = "business.name", target = "businessName")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "brand.name", target = "brandName")
    @Mapping(source = "promotionType", target = "promotionType", qualifiedByName = "productPromotionTypeToString")
    @Mapping(target = "displayPrice", expression = "java(product.getDisplayPrice())")
    @Mapping(target = "isFavorited", constant = "false")
    @AfterMapping
    default void afterListMapping(@MappingTarget ProductListDto dto, Product product) {
        boolean hasSizes = product.getSizes() != null && !product.getSizes().isEmpty();
        dto.setHasSizes(hasSizes);
        if (dto.getIsFavorited() == null) {
            dto.setIsFavorited(false);
        }
        if (product.getBusiness() != null) {
            dto.setBusinessName(product.getBusiness().getName());
        }
        if (product.getCategory() != null) {
            dto.setCategoryName(product.getCategory().getName());
        }
        if (product.getBrand() != null) {
            dto.setBrandName(product.getBrand().getName());
        }
        setDisplayFieldsForList(dto, product);
    }
    ProductListDto toListDto(Product product);

    List<ProductListDto> toListDtos(List<Product> products);

    @Mapping(source = "business.name", target = "businessName")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "brand.name", target = "brandName")
    @Mapping(source = "promotionType", target = "promotionType", qualifiedByName = "productPromotionTypeToString")
    @Mapping(target = "displayPrice", expression = "java(product.getDisplayPrice())")
    @Mapping(target = "isFavorited", constant = "false")
    @AfterMapping
    default void afterDetailMapping(@MappingTarget ProductDetailDto dto, Product product) {
        if (product.getBusiness() != null) {
            dto.setBusinessName(product.getBusiness().getName());
        }
        if (product.getCategory() != null) {
            dto.setCategoryName(product.getCategory().getName());
        }
        if (product.getBrand() != null) {
            dto.setBrandName(product.getBrand().getName());
        }
        boolean hasSizes = product.getSizes() != null && !product.getSizes().isEmpty();
        dto.setHasSizes(hasSizes);
        if (dto.getIsFavorited() == null) {
            dto.setIsFavorited(false);
        }
        setDisplayFieldsForDetail(dto, product);
    }
    ProductDetailDto toDetailDto(Product product);

    default void setDisplayFieldsForList(ProductListDto dto, Product product) {
        try {
            if (product.hasSizes() && product.getSizes() != null && !product.getSizes().isEmpty()) {
                var sizeWithPromotion = product.getSizes().stream()
                        .filter(size -> size != null && size.isPromotionActive())
                        .findFirst();
                
                if (sizeWithPromotion.isPresent()) {
                    var size = sizeWithPromotion.get();
                    dto.setDisplayOriginPrice(size.getPrice());
                    dto.setDisplayPromotionType(size.getPromotionType() != null ? size.getPromotionType().name() : null);
                    dto.setDisplayPromotionValue(size.getPromotionValue());
                    dto.setDisplayPromotionFromDate(size.getPromotionFromDate());
                    dto.setDisplayPromotionToDate(size.getPromotionToDate());
                    dto.setHasPromotion(true);
                } else {
                    var smallestPriceSize = product.getSizes().stream()
                            .filter(size -> size != null && size.getPrice() != null)
                            .min((s1, s2) -> s1.getPrice().compareTo(s2.getPrice()));
                    
                    if (smallestPriceSize.isPresent()) {
                        var size = smallestPriceSize.get();
                        dto.setDisplayOriginPrice(size.getPrice());
                        dto.setDisplayPromotionType(size.getPromotionType() != null ? size.getPromotionType().name() : null);
                        dto.setDisplayPromotionValue(size.getPromotionValue());
                        dto.setDisplayPromotionFromDate(size.getPromotionFromDate());
                        dto.setDisplayPromotionToDate(size.getPromotionToDate());
                        dto.setHasPromotion(size.isPromotionActive());
                    } else {
                        setProductDataAsDisplay(dto, product);
                    }
                }
            } else {
                setProductDataAsDisplay(dto, product);
            }
        } catch (Exception e) {
            setProductDataAsDisplay(dto, product);
        }
    }
    
    default void setDisplayFieldsForDetail(ProductDetailDto dto, Product product) {
        try {
            if (product.hasSizes() && product.getSizes() != null && !product.getSizes().isEmpty()) {
                var sizeWithPromotion = product.getSizes().stream()
                        .filter(size -> size != null && size.isPromotionActive())
                        .findFirst();
                
                if (sizeWithPromotion.isPresent()) {
                    var size = sizeWithPromotion.get();
                    dto.setDisplayOriginPrice(size.getPrice());
                    dto.setDisplayPromotionType(size.getPromotionType() != null ? size.getPromotionType().name() : null);
                    dto.setDisplayPromotionValue(size.getPromotionValue());
                    dto.setDisplayPromotionFromDate(size.getPromotionFromDate());
                    dto.setDisplayPromotionToDate(size.getPromotionToDate());
                    dto.setHasPromotion(true);
                } else {
                    var smallestPriceSize = product.getSizes().stream()
                            .filter(size -> size != null && size.getPrice() != null)
                            .min((s1, s2) -> s1.getPrice().compareTo(s2.getPrice()));
                    
                    if (smallestPriceSize.isPresent()) {
                        var size = smallestPriceSize.get();
                        dto.setDisplayOriginPrice(size.getPrice());
                        dto.setDisplayPromotionType(size.getPromotionType() != null ? size.getPromotionType().name() : null);
                        dto.setDisplayPromotionValue(size.getPromotionValue());
                        dto.setDisplayPromotionFromDate(size.getPromotionFromDate());
                        dto.setDisplayPromotionToDate(size.getPromotionToDate());
                        dto.setHasPromotion(size.isPromotionActive());
                    } else {
                        setProductDataAsDisplayForDetail(dto, product);
                    }
                }
            } else {
                setProductDataAsDisplayForDetail(dto, product);
            }
        } catch (Exception e) {
            setProductDataAsDisplayForDetail(dto, product);
        }
    }
    
    default void setProductDataAsDisplay(ProductListDto dto, Product product) {
        dto.setDisplayOriginPrice(product.getPrice());
        dto.setDisplayPromotionType(product.getPromotionType() != null ? product.getPromotionType().name() : null);
        dto.setDisplayPromotionValue(product.getPromotionValue());
        dto.setDisplayPromotionFromDate(product.getPromotionFromDate());
        dto.setDisplayPromotionToDate(product.getPromotionToDate());
        dto.setHasPromotion(product.isPromotionActive());
    }
    
    default void setProductDataAsDisplayForDetail(ProductDetailDto dto, Product product) {
        dto.setDisplayOriginPrice(product.getPrice());
        dto.setDisplayPromotionType(product.getPromotionType() != null ? product.getPromotionType().name() : null);
        dto.setDisplayPromotionValue(product.getPromotionValue());
        dto.setDisplayPromotionFromDate(product.getPromotionFromDate());
        dto.setDisplayPromotionToDate(product.getPromotionToDate());
        dto.setHasPromotion(product.isPromotionActive());
    }

    @Named("productStringToPromotionType")
    default PromotionType productStringToPromotionType(String promotionType) {
        if (promotionType == null || promotionType.trim().isEmpty()) {
            return null;
        }
        try {
            return PromotionType.valueOf(promotionType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Named("productPromotionTypeToString")
    default String productPromotionTypeToString(PromotionType promotionType) {
        return promotionType != null ? promotionType.name() : null;
    }
}