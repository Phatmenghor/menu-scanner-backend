package com.emenu.features.business.mapper;

import com.emenu.features.business.dto.request.CategoryCreateRequest;
import com.emenu.features.business.dto.response.CategoryResponse;
import com.emenu.features.business.dto.update.CategoryUpdateRequest;
import com.emenu.features.business.models.Category;
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
public abstract class CategoryMapper {

    @Autowired
    protected PaginationMapper paginationMapper;

    @Autowired
    protected ProductRepository productRepository;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "business", ignore = true)
    public abstract Category toEntity(CategoryCreateRequest request);

    @Mapping(source = "business.name", target = "businessName")
    public abstract CategoryResponse toResponse(Category category);

    public abstract List<CategoryResponse> toResponseList(List<Category> categories);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "business", ignore = true)
    public abstract void updateEntity(CategoryUpdateRequest request, @MappingTarget Category category);

    public List<CategoryResponse> toResponseListWithCounts(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return List.of();
        }

        // Get all category IDs
        List<UUID> categoryIds = categories.stream()
                .map(Category::getId)
                .toList();

        // Two GROUP BY queries for all counts (2 queries total instead of N*2)
        Map<UUID, Long> totalProductCounts = productRepository.countByCategoryIds(categoryIds);
        Map<UUID, Long> activeProductCounts = productRepository.countActiveByCategoryIds(categoryIds);

        // Map to responses and set counts
        List<CategoryResponse> responses = toResponseList(categories);
        responses.forEach(response -> {
            Long totalCount = totalProductCounts.getOrDefault(response.getId(), 0L);
            Long activeCount = activeProductCounts.getOrDefault(response.getId(), 0L);
            response.setTotalProducts(totalCount);
            response.setActiveProducts(activeCount);
        });

        return responses;
    }

    public PaginationResponse<CategoryResponse> toPaginationResponse(Page<Category> categoryPage) {
        // Use optimized batch counting
        List<CategoryResponse> responses = toResponseListWithCounts(categoryPage.getContent());
        return paginationMapper.toPaginationResponse(categoryPage, responses);
    }

    // ⚡ PUBLIC: Simple pagination without product counts (FAST!)
    public PaginationResponse<CategoryResponse> toPaginationResponseSimple(Page<Category> categoryPage, List<CategoryResponse> responses) {
        return paginationMapper.toPaginationResponse(categoryPage, responses);
    }
}