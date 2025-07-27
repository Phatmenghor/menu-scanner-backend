package com.emenu.features.product.mapper;

import com.emenu.features.product.dto.request.BrandCreateRequest;
import com.emenu.features.product.dto.response.BrandResponse;
import com.emenu.features.product.dto.update.BrandUpdateRequest;
import com.emenu.features.product.models.Brand;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class BrandMapper {

    @Autowired
    protected PaginationMapper paginationMapper;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true) // Will be set from current user
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    public abstract Brand toEntity(BrandCreateRequest request);

    @Mapping(source = "business.name", target = "businessName")
    public abstract BrandResponse toResponse(Brand brand);

    public abstract List<BrandResponse> toResponseList(List<Brand> brands);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    public abstract void updateEntity(BrandUpdateRequest request, @MappingTarget Brand brand);

    @AfterMapping
    protected void setCalculatedFields(@MappingTarget BrandResponse response, Brand brand) {
        // Set total products count
        if (brand.getProducts() != null) {
            response.setTotalProducts((long) brand.getProducts().size());
            response.setActiveProducts(brand.getProducts().stream()
                    .filter(product -> !product.getIsDeleted() && product.isActive())
                    .count());
        } else {
            response.setTotalProducts(0L);
            response.setActiveProducts(0L);
        }
    }

    public PaginationResponse<BrandResponse> toPaginationResponse(Page<Brand> brandPage) {
        return paginationMapper.toPaginationResponse(brandPage, this::toResponseList);
    }
}