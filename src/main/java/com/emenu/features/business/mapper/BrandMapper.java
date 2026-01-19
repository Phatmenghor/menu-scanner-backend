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

@Mapper(componentModel = "spring", uses = {PaginationMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BrandMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "products", ignore = true)
    Brand toEntity(BrandCreateRequest request);

    @Mapping(source = "business.name", target = "businessName")
    BrandResponse toResponse(Brand brand);

    List<BrandResponse> toResponseList(List<Brand> brands);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "products", ignore = true)
    void updateEntity(BrandUpdateRequest request, @MappingTarget Brand brand);

    default PaginationResponse<BrandResponse> toPaginationResponse(Page<Brand> brandPage, PaginationMapper paginationMapper) {
        return paginationMapper.toPaginationResponse(brandPage, this::toResponseList);
    }
}