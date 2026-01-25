package com.emenu.features.main.mapper;

import com.emenu.features.main.dto.request.BrandCreateRequest;
import com.emenu.features.main.dto.response.BrandResponse;
import com.emenu.features.main.dto.update.BrandUpdateRequest;
import com.emenu.features.main.models.Brand;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PaginationMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BrandMapper {

    @Mapping    @Mapping    @Mapping    @Mapping(target = "products", ignore = true)
    Brand toEntity(BrandCreateRequest request);

    @Mapping(source = "business.name", target = "businessName")
    BrandResponse toResponse(Brand brand);

    List<BrandResponse> toResponseList(List<Brand> brands);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping    @Mapping    @Mapping    @Mapping(target = "products", ignore = true)
    void updateEntity(BrandUpdateRequest request, @MappingTarget Brand brand);

    default PaginationResponse<BrandResponse> toPaginationResponse(Page<Brand> brandPage, PaginationMapper paginationMapper) {
return paginationMapper.toPaginationResponse(brandPage, this::toResponseList);
    }
}