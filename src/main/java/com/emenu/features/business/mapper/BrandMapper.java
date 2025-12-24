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
import java.util.Map;
import java.util.UUID;

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

    public List<BrandResponse> toResponseListWithCounts(List<Brand> brands) {
        if (brands == null || brands.isEmpty()) {
            return List.of();
        }

        // Get all brand IDs
        List<UUID> brandIds = brands.stream()
                .map(Brand::getId)
                .toList();

        Map<UUID, Long> totalProductCounts = productRepository.countByBrandIds(brandIds);
        Map<UUID, Long> activeProductCounts = productRepository.countActiveByBrandIds(brandIds);

        // Map to responses and set counts
        List<BrandResponse> responses = toResponseList(brands);
        responses.forEach(response -> {
            Long totalCount = totalProductCounts.getOrDefault(response.getId(), 0L);
            Long activeCount = activeProductCounts.getOrDefault(response.getId(), 0L);
            response.setTotalProducts(totalCount);
            response.setActiveProducts(activeCount);
        });

        return responses;
    }

    public PaginationResponse<BrandResponse> toPaginationResponse(Page<Brand> brandPage) {
        // Use optimized batch counting
        List<BrandResponse> responses = toResponseListWithCounts(brandPage.getContent());
        return paginationMapper.toPaginationResponse(brandPage, responses);
    }
}