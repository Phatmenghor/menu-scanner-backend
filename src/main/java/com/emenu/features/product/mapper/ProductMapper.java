package com.emenu.features.product.mapper;

import com.emenu.features.product.dto.request.ProductCreateDto;
import com.emenu.features.product.dto.response.ProductDetailDto;
import com.emenu.features.product.dto.response.ProductListDto;
import com.emenu.features.product.models.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring",
        uses = {ProductImageMapper.class, ProductSizeMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ProductMapper {

    @Autowired
    protected ProductFavoriteService favoriteService; // Inject for favorites check

    // ================================
    // ENTITY CREATION
    // ================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true) // Set in service
    @Mapping(target = "viewCount", constant = "0L")
    @Mapping(target = "favoriteCount", constant = "0L")
    @Mapping(target = "images", ignore = true) // Handle in service
    @Mapping(target = "sizes", ignore = true) // Handle in service
    @Mapping(source = "promotionType", target = "promotionType", qualifiedByName = "stringToPromotionType")
    public abstract Product toEntity(ProductCreateDto dto);

    // ================================
    // LIST DTO MAPPING (Optimized for listings)
    // ================================

    @Mapping(source = "business.name", target = "businessName")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "brand.name", target = "brandName")
    @Mapping(target = "displayPrice", expression = "java(product.getDisplayPrice())")
    @Mapping(target = "hasPromotion", expression = "java(product.isPromotionActive())")
    @Mapping(target = "hasSizes", expression = "java(product.hasSizes())")
    @Mapping(target = "mainImageUrl", expression = "java(product.getMainImageUrl())")
    @Mapping(target = "isFavorited", constant = "false") // Set separately in service
    public abstract ProductListDto toListDto(Product product);

    public abstract List<ProductListDto> toListDtos(List<Product> products);

    // ================================
    // DETAIL DTO MAPPING (Full data for single product)
    // ================================

    @Mapping(source = "business.name", target = "businessName")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "brand.name", target = "brandName")
    @Mapping(target = "displayPrice", expression = "java(product.getDisplayPrice())")
    @Mapping(target = "hasPromotion", expression = "java(product.isPromotionActive())")
    @Mapping(target = "hasSizes", expression = "java(product.hasSizes())")
    @Mapping(source = "promotionType", target = "promotionType", qualifiedByName = "promotionTypeToString")
    @Mapping(target = "isFavorited", constant = "false") // Set separately in service
    public abstract ProductDetailDto toDetailDto(Product product);

    // ================================
    // FAVORITE STATUS ENRICHMENT
    // ================================

    /**
     * 🚀 OPTIMIZED: Enrich list with favorite status in batch
     */
    public List<ProductListDto> enrichWithFavorites(List<ProductListDto> products, UUID userId) {
        if (userId == null || products.isEmpty()) {
            return products;
        }

        List<UUID> productIds = products.stream()
                .map(ProductListDto::getId)
                .toList();

        // Batch query for favorites
        List<UUID> favoriteProductIds = favoriteService.getFavoriteProductIds(userId, productIds);
        
        products.forEach(product -> 
            product.setIsFavorited(favoriteProductIds.contains(product.getId())));

        return products;
    }

    /**
     * 🚀 OPTIMIZED: Enrich single product with favorite status
     */
    public ProductDetailDto enrichWithFavorite(ProductDetailDto product, UUID userId) {
        if (userId != null) {
            boolean isFavorited = favoriteService.isFavorited(userId, product.getId());
            product.setIsFavorited(isFavorited);
        }
        return product;
    }

    // ================================
    // HELPER METHODS
    // ================================

    @Named("stringToPromotionType")
    protected com.emenu.enums.product.PromotionType stringToPromotionType(String promotionType) {
        if (promotionType == null || promotionType.trim().isEmpty()) {
            return null;
        }
        try {
            return com.emenu.enums.product.PromotionType.valueOf(promotionType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Named("promotionTypeToString")
    protected String promotionTypeToString(com.emenu.enums.product.PromotionType promotionType) {
        return promotionType != null ? promotionType.name() : null;
    }
}