package com.emenu.features.product.mapper;

import com.emenu.enums.product.PromotionType;
import com.emenu.features.product.dto.request.ProductCreateDto;
import com.emenu.features.product.dto.response.ProductDetailDto;
import com.emenu.features.product.dto.response.ProductListDto;
import com.emenu.features.product.dto.update.ProductUpdateDto;
import com.emenu.features.product.models.Product;
import com.emenu.features.product.models.ProductSize;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        uses = {ProductImageMapper.class, ProductSizeMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    // Create entity from DTO
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

    // Update entity from DTO
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

    // For listing
    @Mapping(source = "business.name", target = "businessName")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "brand.name", target = "brandName")
    
    // ✅ Original fields from product table (always from database)
    @Mapping(source = "price", target = "price")
    @Mapping(source = "promotionType", target = "promotionType", qualifiedByName = "promotionTypeToString")
    @Mapping(source = "promotionValue", target = "promotionValue")
    @Mapping(source = "promotionFromDate", target = "promotionFromDate")
    @Mapping(source = "promotionToDate", target = "promotionToDate")
    
    // Calculated fields
    @Mapping(target = "displayPrice", expression = "java(product.getDisplayPrice())")
    @Mapping(target = "hasSizes", expression = "java(product.hasSizes())")
    @Mapping(target = "mainImageUrl", expression = "java(product.getMainImageUrl())")
    @Mapping(target = "isFavorited", constant = "false")
    @AfterMapping
    default void afterListMapping(@MappingTarget ProductListDto dto, Product product) {
        // ✅ ALWAYS set display fields using conditional logic
        setDisplayFieldsForList(dto, product);
    }
    ProductListDto toListDto(Product product);

    List<ProductListDto> toListDtos(List<Product> products);

    // For detail
    @Mapping(source = "business.name", target = "businessName")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "brand.name", target = "brandName")
    
    // ✅ Original fields from product table (always from database)
    @Mapping(source = "price", target = "price")
    @Mapping(source = "promotionType", target = "promotionType", qualifiedByName = "promotionTypeToString")
    @Mapping(source = "promotionValue", target = "promotionValue")
    @Mapping(source = "promotionFromDate", target = "promotionFromDate")
    @Mapping(source = "promotionToDate", target = "promotionToDate")
    
    // Calculated fields
    @Mapping(target = "displayPrice", expression = "java(product.getDisplayPrice())")
    @Mapping(target = "hasSizes", expression = "java(product.hasSizes())")
    @Mapping(target = "isFavorited", constant = "false")
    @AfterMapping
    default void afterDetailMapping(@MappingTarget ProductDetailDto dto, Product product) {
        // ✅ ALWAYS set display fields using conditional logic
        setDisplayFieldsForDetail(dto, product);
    }
    ProductDetailDto toDetailDto(Product product);

    // ================================
    // ✅ CONDITIONAL LOGIC FOR DISPLAY FIELDS
    // ================================
    
    default void setDisplayFieldsForList(ProductListDto dto, Product product) {
        if (product.hasSizes() && !product.getSizes().isEmpty()) {
            // When has sizes: use conditional logic for display fields
            
            // First try to find a size with active promotion
            var sizeWithPromotion = product.getSizes().stream()
                    .filter(ProductSize::isPromotionActive)
                    .findFirst();
            
            if (sizeWithPromotion.isPresent()) {
                // Use first size with promotion for display fields
                var size = sizeWithPromotion.get();
                dto.setDisplayOriginPrice(size.getPrice());
                dto.setDisplayPromotionType(promotionTypeToString(size.getPromotionType()));
                dto.setDisplayPromotionValue(size.getPromotionValue());
                dto.setDisplayPromotionFromDate(size.getPromotionFromDate());
                dto.setDisplayPromotionToDate(size.getPromotionToDate());
                dto.setHasPromotion(true);
            } else {
                // No size has promotion, use size with smallest price for display fields
                var smallestPriceSize = product.getSizes().stream()
                        .min((s1, s2) -> s1.getPrice().compareTo(s2.getPrice()));
                
                if (smallestPriceSize.isPresent()) {
                    var size = smallestPriceSize.get();
                    dto.setDisplayOriginPrice(size.getPrice());
                    dto.setDisplayPromotionType(promotionTypeToString(size.getPromotionType()));
                    dto.setDisplayPromotionValue(size.getPromotionValue());
                    dto.setDisplayPromotionFromDate(size.getPromotionFromDate());
                    dto.setDisplayPromotionToDate(size.getPromotionToDate());
                    dto.setHasPromotion(size.isPromotionActive());
                }
            }
        } else {
            // When no sizes: display fields also use conditional logic (same as product data)
            dto.setDisplayOriginPrice(product.getPrice());
            dto.setDisplayPromotionType(promotionTypeToString(product.getPromotionType()));
            dto.setDisplayPromotionValue(product.getPromotionValue());
            dto.setDisplayPromotionFromDate(product.getPromotionFromDate());
            dto.setDisplayPromotionToDate(product.getPromotionToDate());
            dto.setHasPromotion(product.isPromotionActive());
        }
    }
    
    default void setDisplayFieldsForDetail(ProductDetailDto dto, Product product) {
        if (product.hasSizes() && !product.getSizes().isEmpty()) {
            // When has sizes: use conditional logic for display fields
            
            // First try to find a size with active promotion
            var sizeWithPromotion = product.getSizes().stream()
                    .filter(ProductSize::isPromotionActive)
                    .findFirst();
            
            if (sizeWithPromotion.isPresent()) {
                // Use first size with promotion for display fields
                var size = sizeWithPromotion.get();
                dto.setDisplayOriginPrice(size.getPrice());
                dto.setDisplayPromotionType(promotionTypeToString(size.getPromotionType()));
                dto.setDisplayPromotionValue(size.getPromotionValue());
                dto.setDisplayPromotionFromDate(size.getPromotionFromDate());
                dto.setDisplayPromotionToDate(size.getPromotionToDate());
                dto.setHasPromotion(true);
            } else {
                // No size has promotion, use size with smallest price for display fields
                var smallestPriceSize = product.getSizes().stream()
                        .min((s1, s2) -> s1.getPrice().compareTo(s2.getPrice()));
                
                if (smallestPriceSize.isPresent()) {
                    var size = smallestPriceSize.get();
                    dto.setDisplayOriginPrice(size.getPrice());
                    dto.setDisplayPromotionType(promotionTypeToString(size.getPromotionType()));
                    dto.setDisplayPromotionValue(size.getPromotionValue());
                    dto.setDisplayPromotionFromDate(size.getPromotionFromDate());
                    dto.setDisplayPromotionToDate(size.getPromotionToDate());
                    dto.setHasPromotion(size.isPromotionActive());
                }
            }
        } else {
            // When no sizes: display fields also use conditional logic (same as product data)
            dto.setDisplayOriginPrice(product.getPrice());
            dto.setDisplayPromotionType(promotionTypeToString(product.getPromotionType()));
            dto.setDisplayPromotionValue(product.getPromotionValue());
            dto.setDisplayPromotionFromDate(product.getPromotionFromDate());
            dto.setDisplayPromotionToDate(product.getPromotionToDate());
            dto.setHasPromotion(product.isPromotionActive());
        }
    }

    // ================================
    // CONVERTERS
    // ================================
    
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