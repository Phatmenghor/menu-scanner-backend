package com.emenu.features.product.mapper;

import com.emenu.enums.product.PromotionType;
import com.emenu.features.product.dto.request.ProductCreateDto;
import com.emenu.features.product.dto.response.ProductDetailDto;
import com.emenu.features.product.dto.response.ProductListDto;
import com.emenu.features.product.dto.update.ProductUpdateDto;
import com.emenu.features.product.models.Product;
import com.emenu.features.product.utils.ProductFavoriteQueryHelper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring",
        uses = {ProductImageMapper.class, ProductSizeMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ProductMapper {

    @Autowired
    protected ProductFavoriteQueryHelper favoriteQueryHelper;

    // ================================
    // ENTITY CREATION FROM CREATE DTO
    // ================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true) // Set in service
    @Mapping(target = "viewCount", constant = "0L")
    @Mapping(target = "favoriteCount", constant = "0L")
    @Mapping(target = "images", ignore = true) // Handle in service
    @Mapping(target = "sizes", ignore = true) // Handle in service
    @Mapping(source = "promotionType", target = "promotionType", qualifiedByName = "productStringToPromotionType")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    public abstract Product toEntity(ProductCreateDto dto);

    // ================================
    // ENTITY UPDATE FROM UPDATE DTO
    // ================================

    @Mapping(target = "id", ignore = true) // Keep existing ID
    @Mapping(target = "businessId", ignore = true) // Keep existing business
    @Mapping(target = "viewCount", ignore = true) // Keep existing count
    @Mapping(target = "favoriteCount", ignore = true) // Keep existing count
    @Mapping(target = "images", ignore = true) // Handle in service
    @Mapping(target = "sizes", ignore = true) // Handle in service
    @Mapping(source = "promotionType", target = "promotionType", qualifiedByName = "productStringToPromotionType")
    @Mapping(target = "createdAt", ignore = true) // Keep original
    @Mapping(target = "updatedAt", ignore = true) // Auto-updated by JPA
    @Mapping(target = "createdBy", ignore = true) // Keep original
    @Mapping(target = "updatedBy", ignore = true) // Auto-updated by JPA auditing
    @Mapping(target = "isDeleted", ignore = true) // Keep existing
    @Mapping(target = "deletedAt", ignore = true) // Keep existing
    @Mapping(target = "deletedBy", ignore = true) // Keep existing
    public abstract void updateEntityFromDto(ProductUpdateDto dto, @MappingTarget Product entity);

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
    @Mapping(source = "promotionType", target = "promotionType", qualifiedByName = "productPromotionTypeToString")
    @Mapping(target = "isFavorited", constant = "false") // Set separately in service
    public abstract ProductDetailDto toDetailDto(Product product);

    // ================================
    // FAVORITE STATUS ENRICHMENT
    // ================================

    /**
     * Enrich list with favorite status in batch
     */
    public List<ProductListDto> enrichWithFavorites(List<ProductListDto> products, UUID userId) {
        if (userId == null || products.isEmpty()) {
            return products;
        }

        List<UUID> productIds = products.stream()
                .map(ProductListDto::getId)
                .toList();

        // Batch query for favorites
        List<UUID> favoriteProductIds = favoriteQueryHelper.getFavoriteProductIds(userId, productIds);
        
        products.forEach(product -> 
            product.setIsFavorited(favoriteProductIds.contains(product.getId())));

        return products;
    }

    /**
     * Enrich single product with favorite status
     */
    public ProductDetailDto enrichWithFavorite(ProductDetailDto product, UUID userId) {
        if (userId != null) {
            boolean isFavorited = favoriteQueryHelper.isFavorited(userId, product.getId());
            product.setIsFavorited(isFavorited);
        }
        return product;
    }

    // ================================
    // PROMOTION HANDLING HELPERS - UNIQUE NAMES
    // ================================

    @Named("productStringToPromotionType")
    protected PromotionType productStringToPromotionType(String promotionType) {
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
    protected String productPromotionTypeToString(PromotionType promotionType) {
        return promotionType != null ? promotionType.name() : null;
    }

    // ================================
    // UPDATE HELPERS
    // ================================

    /**
     * Custom update method that preserves certain fields
     */
    @AfterMapping
    protected void afterUpdateMapping(ProductUpdateDto dto, @MappingTarget Product entity) {
        // Handle promotion clearing
        if (!dto.hasPromotionData()) {
            entity.setPromotionType(null);
            entity.setPromotionValue(null);
            entity.setPromotionFromDate(null);
            entity.setPromotionToDate(null);
        }
    }
}