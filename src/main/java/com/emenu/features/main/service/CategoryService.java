package com.emenu.features.main.service;

import com.emenu.features.main.dto.filter.CategoryAllFilterRequest;
import com.emenu.features.main.dto.filter.CategoryFilterRequest;
import com.emenu.features.main.dto.request.CategoryCreateRequest;
import com.emenu.features.main.dto.response.CategoryResponse;
import com.emenu.features.main.dto.update.CategoryUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    
    // CRUD Operations
    CategoryResponse createCategory(CategoryCreateRequest request);
    PaginationResponse<CategoryResponse> getAllCategories(CategoryFilterRequest filter);
    List<CategoryResponse> getAllItemCategories(CategoryAllFilterRequest filter);
    CategoryResponse getCategoryById(UUID id);
    CategoryResponse updateCategory(UUID id, CategoryUpdateRequest request);
    CategoryResponse deleteCategory(UUID id);
}