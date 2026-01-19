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

@Mapper(componentModel = "spring", uses = {PaginationMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "business", ignore = true)
    Category toEntity(CategoryCreateRequest request);

    @Mapping(source = "business.name", target = "businessName")
    CategoryResponse toResponse(Category category);

    List<CategoryResponse> toResponseList(List<Category> categories);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "business", ignore = true)
    void updateEntity(CategoryUpdateRequest request, @MappingTarget Category category);

    default PaginationResponse<CategoryResponse> toPaginationResponse(Page<Category> categoryPage, PaginationMapper paginationMapper) {
        return paginationMapper.toPaginationResponse(categoryPage, this::toResponseList);
    }
}