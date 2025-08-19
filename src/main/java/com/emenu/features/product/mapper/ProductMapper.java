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
    @Mapping(source = "promotionType", target = "promotionType", qualifiedByName = "stringToPromotionType")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    Product toEntity(ProductCreateDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "favoriteCount", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "sizes", ignore = true)
    @Mapping(source = "promotionType", target = "promotionType", qualifiedByName = "stringToPromotionType")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @AfterMapping
    default void afterUpdateMapping(ProductUpdateDto dto, @MappingTarget Product entity) {
        if (!dto.hasPromotionData()) {
            entity.setPromotionType(null);
            entity.setPromotionValue(null);
            entity.setPromotionFromDate(null);
            entity.setPromotionToDate(null);
        }
    }
    void updateEntityFromDto(ProductUpdateDto dto, @MappingTarget Product entity);

    // ✅ FIXED: Enhanced mapping with proper null handling
    @Mapping(source = "business.name", target = "businessName", 
             nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    @Mapping(source = "category.name", target = "categoryName",
             nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    @Mapping(source = "brand.name", target = "brandName",
             nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    @Mapping(source = "price", target = "price")
    @Mapping(source = "promotionType", target = "promotionType", qualifiedByName = "promotionTypeToString")
    @Mapping(source = "promotionValue", target = "promotionValue")
    @Mapping(source = "promotionFromDate", target = "promotionFromDate")
    @Mapping(source = "promotionToDate", target = "promotionToDate")
    @Mapping(target = "displayPrice", expression = "java(product.getDisplayPrice())")
    @Mapping(target = "mainImageUrl", expression = "java(product.getMainImageUrl())")
    @Mapping(target = "isFavorited", constant = "false")
    @AfterMapping
    default void afterListMapping(@MappingTarget ProductListDto dto, Product product) {
        // ✅ FIXED: Enhanced null-safe relationship mapping
        if (product.getBusiness() != null) {
            dto.setBusinessName(product.getBusiness().getName());
        }
        if (product.getCategory() != null) {
            dto.setCategoryName(product.getCategory().getName());
        }
        if (product.getBrand() != null) {
            dto.setBrandName(product.getBrand().getName());
        }

        // Fix hasSizes calculation
        boolean hasSizes = product.getSizes() != null && !product.getSizes().isEmpty();
        dto.setHasSizes(hasSizes);

        // Fix null boolean fields
        if (dto.getIsFavorited() == null) {
            dto.setIsFavorited(false);
        }

        setDisplayFieldsForList(dto, product);
    }
    ProductListDto toListDto(Product product);

    List<ProductListDto> toListDtos(List<Product> products);

    // ✅ FIXED: Enhanced detail mapping with proper null handling
    @Mapping(source = "business.name", target = "businessName",
             nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    @Mapping(source = "category.name", target = "categoryName",
             nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    @Mapping(source = "brand.name", target = "brandName",
             nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    @Mapping(source = "price", target = "price")
    @Mapping(source = "promotionType", target = "promotionType", qualifiedByName = "promotionTypeToString")
    @Mapping(source = "promotionValue", target = "promotionValue")
    @Mapping(source = "promotionFromDate", target = "promotionFromDate")
    @Mapping(source = "promotionToDate", target = "promotionToDate")
    @Mapping(target = "displayPrice", expression = "java(product.getDisplayPrice())")
    @Mapping(target = "isFavorited", constant = "false")
    @AfterMapping
    default void afterDetailMapping(@MappingTarget ProductDetailDto dto, Product product) {
        // ✅ FIXED: Enhanced null-safe relationship mapping
        if (product.getBusiness() != null) {
            dto.setBusinessName(product.getBusiness().getName());
        }
        if (product.getCategory() != null) {
            dto.setCategoryName(product.getCategory().getName());
        }
        if (product.getBrand() != null) {
            dto.setBrandName(product.getBrand().getName());
        }

        // Fix hasSizes calculation
        boolean hasSizes = product.getSizes() != null && !product.getSizes().isEmpty();
        dto.setHasSizes(hasSizes);

        // Fix null boolean fields
        if (dto.getIsFavorited() == null) {
            dto.setIsFavorited(false);
        }

        setDisplayFieldsForDetail(dto, product);
    }
    ProductDetailDto toDetailDto(Product product);

    // ✅ FIXED: Enhanced display field logic with null safety
    default void setDisplayFieldsForList(ProductListDto dto, Product product) {
        try {
            if (product.hasSizes() && product.getSizes() != null && !product.getSizes().isEmpty()) {
                var sizeWithPromotion = product.getSizes().stream()
                        .filter(size -> size != null && size.isPromotionActive())
                        .findFirst();
                
                if (sizeWithPromotion.isPresent()) {
                    var size = sizeWithPromotion.get();
                    dto.setDisplayOriginPrice(size.getPrice());
                    dto.setDisplayPromotionType(promotionTypeToString(size.getPromotionType()));
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
                        dto.setDisplayPromotionType(promotionTypeToString(size.getPromotionType()));
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
            // ✅ FALLBACK: Use product data if size processing fails
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
                    dto.setDisplayPromotionType(promotionTypeToString(size.getPromotionType()));
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
                        dto.setDisplayPromotionType(promotionTypeToString(size.getPromotionType()));
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
            // ✅ FALLBACK: Use product data if size processing fails
            setProductDataAsDisplayForDetail(dto, product);
        }
    }
    
    default void setProductDataAsDisplay(ProductListDto dto, Product product) {
        dto.setDisplayOriginPrice(product.getPrice());
        dto.setDisplayPromotionType(promotionTypeToString(product.getPromotionType()));
        dto.setDisplayPromotionValue(product.getPromotionValue());
        dto.setDisplayPromotionFromDate(product.getPromotionFromDate());
        dto.setDisplayPromotionToDate(product.getPromotionToDate());
        dto.setHasPromotion(product.isPromotionActive());
    }
    
    default void setProductDataAsDisplayForDetail(ProductDetailDto dto, Product product) {
        dto.setDisplayOriginPrice(product.getPrice());
        dto.setDisplayPromotionType(promotionTypeToString(product.getPromotionType()));
        dto.setDisplayPromotionValue(product.getPromotionValue());
        dto.setDisplayPromotionFromDate(product.getPromotionFromDate());
        dto.setDisplayPromotionToDate(product.getPromotionToDate());
        dto.setHasPromotion(product.isPromotionActive());
    }

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
}