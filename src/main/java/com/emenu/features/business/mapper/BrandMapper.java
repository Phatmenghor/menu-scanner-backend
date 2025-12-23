package com.emenu.features.business.mapper;

import com.emenu.features.business.dto.request.BrandCreateRequest;
import com.emenu.features.business.dto.response.BrandResponse;
import com.emenu.features.business.dto.update.BrandUpdateRequest;
import com.emenu.features.business.models.Brand;
import com.emenu.features.product.repository.ProductRepository;
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

    @Autowired
    protected ProductRepository productRepository;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "products", ignore = true)
    public abstract Brand toEntity(BrandCreateRequest request);

    @Mapping(source = "business.name", target = "businessName")
    public abstract BrandResponse toResponse(Brand brand);

    public abstract List<BrandResponse> toResponseList(List<Brand> brands);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "products", ignore = true)
    public abstract void updateEntity(BrandUpdateRequest request, @MappingTarget Brand brand);

    @AfterMapping
    protected void setCalculatedFields(@MappingTarget BrandResponse response, Brand brand) {
        if (brand.getId() != null) {
            try {
                long totalProducts = productRepository.countByBrandId(brand.getId());
                response.setTotalProducts(totalProducts);
                response.setActiveProducts(totalProducts);
            } catch (Exception e) {
                // Fallback to 0 if there's an error
                response.setTotalProducts(0L);
                response.setActiveProducts(0L);
            }
        } else {
            response.setTotalProducts(0L);
            response.setActiveProducts(0L);
        }
    }

    public PaginationResponse<BrandResponse> toPaginationResponse(Page<Brand> brandPage) {
        return paginationMapper.toPaginationResponse(brandPage, this::toResponseList);
    }
}