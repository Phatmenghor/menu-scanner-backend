package com.emenu.features.business.controller;

import com.emenu.features.auth.models.User;
import com.emenu.features.business.dto.filter.CategoryFilterRequest;
import com.emenu.features.business.dto.response.CategoryResponse;
import com.emenu.features.business.service.CategoryService;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.ApiResponse;
import com.emenu.shared.dto.PaginationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/categories")
@RequiredArgsConstructor
@Slf4j
public class PublicCategoryController {
    private final CategoryService categoryService;
    private final SecurityUtils securityUtils;

    /**
     * Get all categories with filtering (uses current user's business from token)
     */
    @PostMapping("/my-business/all")
    public ResponseEntity<ApiResponse<PaginationResponse<CategoryResponse>>> getMyBusinessAllCategories(@Valid @RequestBody CategoryFilterRequest filter) {
        log.info("Getting my categories for current user's business");
        User currentUser = securityUtils.getCurrentUser();
        filter.setBusinessId(currentUser.getBusinessId());
        PaginationResponse<CategoryResponse> categories = categoryService.getAllCategories(filter);
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", categories));
    }
}
