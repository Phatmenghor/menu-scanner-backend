package com.emenu.features.business.service.impl;

import com.emenu.exception.custom.NotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.models.User;
import com.emenu.features.business.dto.filter.CategoryAllFilterRequest;
import com.emenu.features.business.dto.filter.CategoryFilterRequest;
import com.emenu.features.business.dto.request.CategoryCreateRequest;
import com.emenu.features.business.dto.response.CategoryResponse;
import com.emenu.features.business.dto.update.CategoryUpdateRequest;
import com.emenu.features.business.mapper.CategoryMapper;
import com.emenu.features.business.models.Category;
import com.emenu.features.business.repository.CategoryRepository;
import com.emenu.features.business.service.CategoryService;
import com.emenu.features.business.specification.CategorySpecification;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final SecurityUtils securityUtils;

    @Override
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        log.info("Creating category: {}", request.getName());

        User currentUser = securityUtils.getCurrentUser();
        if (currentUser.getBusinessId() == null) {
            throw new ValidationException("User is not associated with any business");
        }

        // Check if category name already exists for this business
        if (categoryRepository.existsByNameAndBusinessIdAndIsDeletedFalse(
                request.getName(), currentUser.getBusinessId())) {
            throw new ValidationException("Category name already exists in your business");
        }

        Category category = categoryMapper.toEntity(request);
        category.setBusinessId(currentUser.getBusinessId());

        Category savedCategory = categoryRepository.save(category);

        log.info("Category created successfully: {} for business: {}",
                savedCategory.getName(), currentUser.getBusinessId());
        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<CategoryResponse> getAllCategories(CategoryFilterRequest filter) {

        Specification<Category> spec = CategorySpecification.buildSpecification(filter);

        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(
                pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        Page<Category> categoryPage = categoryRepository.findAll(spec, pageable);
        return categoryMapper.toPaginationResponse(categoryPage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllItemCategories(CategoryAllFilterRequest filter) {

        Specification<Category> spec = CategorySpecification.buildSpecification(filter);

        List<Category> categories = categoryRepository.findAll(spec, PaginationUtils.createSort(filter.getSortBy(), filter.getSortDirection()));
        return categoryMapper.toResponseList(categories);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(UUID id) {
        Category category = findCategoryById(id);
        return categoryMapper.toResponse(category);
    }

    @Override
    public CategoryResponse updateCategory(UUID id, CategoryUpdateRequest request) {
        Category category = findCategoryById(id);

        // Check if new name already exists (if name is being changed)
        if (request.getName() != null && !request.getName().equals(category.getName())) {
            if (categoryRepository.existsByNameAndBusinessIdAndIsDeletedFalse(
                    request.getName(), category.getBusinessId())) {
                throw new ValidationException("Category name already exists in your business");
            }
        }

        categoryMapper.updateEntity(request, category);
        Category updatedCategory = categoryRepository.save(category);

        log.info("Category updated successfully: {}", id);
        return categoryMapper.toResponse(updatedCategory);
    }

    @Override
    public CategoryResponse deleteCategory(UUID id) {
        Category category = findCategoryById(id);

        category.softDelete();
        category = categoryRepository.save(category);

        log.info("Category deleted successfully: {}", id);
        return categoryMapper.toResponse(category);
    }

    // Private helper methods
    private Category findCategoryById(UUID id) {
        return categoryRepository.findByIdWithBusiness(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));
    }
}