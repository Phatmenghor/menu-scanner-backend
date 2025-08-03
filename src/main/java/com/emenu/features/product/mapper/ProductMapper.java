package com.emenu.features.product.mapper;

import com.emenu.enums.product.PromotionType;
import com.emenu.features.product.dto.request.ProductCreateRequest;
import com.emenu.features.product.dto.response.ProductResponse;
import com.emenu.features.product.dto.update.ProductUpdateRequest;
import com.emenu.features.product.models.Product;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {ProductSizeMapper.class, ProductImageMapper.class})
public abstract class ProductMapper {

    @Autowired
    protected PaginationMapper paginationMapper;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true) // Will be set from current user
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "images", ignore = true) // Will be handled separately
    @Mapping(target = "sizes", ignore = true) // Will be handled separately
    @Mapping(target = "viewCount", constant = "0L")
    @Mapping(target = "favoriteCount", constant = "0L")
    @Mapping(source = "promotionType", target = "promotionType", qualifiedByName = "mapStringToPromotionType")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    public abstract Product toEntity(ProductCreateRequest request);

    @Mapping(source = "business.name", target = "businessName")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "brand.name", target = "brandName")
    @Mapping(source = "promotionType", target = "promotionType", qualifiedByName = "mapPromotionTypeToString")
    public abstract ProductResponse toResponse(Product product);

    public abstract List<ProductResponse> toResponseList(List<Product> products);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "images", ignore = true) // Will be handled separately
    @Mapping(target = "sizes", ignore = true) // Will be handled separately
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "favoriteCount", ignore = true)
    @Mapping(source = "promotionType", target = "promotionType", qualifiedByName = "mapStringToPromotionType")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    public abstract void updateEntity(ProductUpdateRequest request, @MappingTarget Product product);

    @AfterMapping
    protected void setCalculatedFields(@MappingTarget ProductResponse response, Product product) {
        // Set main image URL
        response.setMainImageUrl(product.getMainImageUrl());

        // Set pricing information
        response.setDisplayPrice(product.getDisplayPrice());
        response.setHasPromotionActive(product.isPromotionActive());
        response.setHasSizes(product.hasSizes());

        // Set public URL (will be implemented based on subdomain)
        response.setPublicUrl("/products/" + product.getId());

        // Set favorite status (will be overridden in service if user is logged in)
        response.setIsFavorited(false);
    }

    @Named("mapStringToPromotionType")
    protected PromotionType mapStringToPromotionType(String promotionType) {
        if (promotionType == null || promotionType.trim().isEmpty()) return null;
        try {
            return PromotionType.valueOf(promotionType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Named("mapPromotionTypeToString")
    protected String mapPromotionTypeToString(PromotionType promotionType) {
        return promotionType != null ? promotionType.name() : null;
    }

    public PaginationResponse<ProductResponse> toPaginationResponse(Page<Product> productPage) {
        return paginationMapper.toPaginationResponse(productPage, this::toResponseList);
    }

}