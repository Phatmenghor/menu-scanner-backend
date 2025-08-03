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

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class CategoryMapper {

    @Autowired
    protected PaginationMapper paginationMapper;

    @Autowired
    protected ProductRepository productRepository;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    public abstract Category toEntity(CategoryCreateRequest request);

    @Mapping(source = "business.name", target = "businessName")
    public abstract CategoryResponse toResponse(Category category);

    public abstract List<CategoryResponse> toResponseList(List<Category> categories);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    public abstract void updateEntity(CategoryUpdateRequest request, @MappingTarget Category category);

    @AfterMapping
    protected void setComputedFields(@MappingTarget CategoryResponse response, Category category) {
        // ✅ FIXED: Actually count products in this category
        if (category.getId() != null) {
            try {
                long productCount = productRepository.countByCategoryId(category.getId());
                response.setTotalProducts(productCount);
            } catch (Exception e) {
                // Fallback to 0 if there's an error
                response.setTotalProducts(0L);
            }
        } else {
            response.setTotalProducts(0L);
        }
    }

    public PaginationResponse<CategoryResponse> toPaginationResponse(Page<Category> categoryPage) {
        return paginationMapper.toPaginationResponse(categoryPage, this::toResponseList);
    }
}