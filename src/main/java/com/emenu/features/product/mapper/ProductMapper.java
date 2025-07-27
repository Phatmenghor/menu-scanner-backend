package com.emenu.features.product.mapper;

import com.emenu.features.product.dto.request.ProductCreateRequest;
import com.emenu.features.product.dto.response.ProductResponse;
import com.emenu.features.product.dto.response.ProductSummaryResponse;
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
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    public abstract Product toEntity(ProductCreateRequest request);

    @Mapping(source = "business.name", target = "businessName")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "brand.name", target = "brandName")
    public abstract ProductResponse toResponse(Product product);

    public abstract List<ProductResponse> toResponseList(List<Product> products);

    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "brand.name", target = "brandName")
    public abstract ProductSummaryResponse toSummaryResponse(Product product);

    public abstract List<ProductSummaryResponse> toSummaryResponseList(List<Product> products);

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
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    public abstract void updateEntity(ProductUpdateRequest request, @MappingTarget Product product);

    @AfterMapping
    protected void setCalculatedFields(@MappingTarget ProductResponse response, Product product) {
        // Set main image URL
        response.setMainImageUrl(product.getMainImageUrl());
        
        // Set pricing information
        response.setStartingPrice(product.getStartingPrice());
        response.setDisplayPrice(product.getStartingPrice()); // Use starting price as display price
        response.setHasPromotion(product.hasPromotion());
        response.setHasMultipleSizes(product.hasMultipleSizes());
        
        // Set public URL (will be implemented based on subdomain)
        response.setPublicUrl("/products/" + product.getId());
        
        // Set favorite status (will be overridden in service if user is logged in)
        response.setIsFavorited(false);
    }

    @AfterMapping
    protected void setSummaryCalculatedFields(@MappingTarget ProductSummaryResponse response, Product product) {
        // Set main image URL
        response.setMainImageUrl(product.getMainImageUrl());
        
        // Set pricing information
        response.setDisplayPrice(product.getStartingPrice());
        response.setHasPromotion(product.hasPromotion());
        response.setHasMultipleSizes(product.hasMultipleSizes());
        
        // Set public URL
        response.setPublicUrl("/products/" + product.getId());
        
        // Set favorite status
        response.setIsFavorited(false);
    }

    public PaginationResponse<ProductResponse> toPaginationResponse(Page<Product> productPage) {
        return paginationMapper.toPaginationResponse(productPage, this::toResponseList);
    }

    public PaginationResponse<ProductSummaryResponse> toSummaryPaginationResponse(Page<Product> productPage) {
        return paginationMapper.toPaginationResponse(productPage, this::toSummaryResponseList);
    }
}