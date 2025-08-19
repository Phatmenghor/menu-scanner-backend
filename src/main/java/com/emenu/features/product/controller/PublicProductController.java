package com.emenu.features.product.controller;

import com.emenu.features.product.dto.filter.ProductFilterDto;
import com.emenu.features.product.dto.response.ProductDetailDto;
import com.emenu.features.product.dto.response.ProductListDto;
import com.emenu.features.product.service.ProductService;
import com.emenu.shared.dto.ApiResponse;
import com.emenu.shared.dto.PaginationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public/products")
@RequiredArgsConstructor
@Slf4j
public class PublicProductController {

    private final ProductService productService;

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PaginationResponse<ProductListDto>>> searchPublicProducts(
            @Valid @RequestBody ProductFilterDto filter) {
        
        log.info("Public search - Page: {}, Size: {}", filter.getPageNo(), filter.getPageSize());
        
        PaginationResponse<ProductListDto> products = productService.getAllProducts(filter);
        
        return ResponseEntity.ok(ApiResponse.success(
            String.format("Found %d products", products.getTotalElements()),
            products
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDetailDto>> getPublicProductById(@PathVariable UUID id) {
        log.info("Getting public product by ID: {}", id);
        
        ProductDetailDto product = productService.getProductByIdPublic(id);
        
        return ResponseEntity.ok(ApiResponse.success("Product retrieved successfully", product));
    }

    @PostMapping("/business/{businessId}")
    public ResponseEntity<ApiResponse<PaginationResponse<ProductListDto>>> getBusinessProducts(
            @PathVariable UUID businessId,
            @Valid @RequestBody ProductFilterDto filter) {
        
        log.info("Getting public business products - Business: {}", businessId);
        
        // Set business ID for filtering
        filter.setBusinessId(businessId);
        
        PaginationResponse<ProductListDto> products = productService.getAllProducts(filter);
        
        return ResponseEntity.ok(ApiResponse.success(
            String.format("Found %d products for business", products.getTotalElements()),
            products
        ));
    }

    @PostMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<PaginationResponse<ProductListDto>>> getCategoryProducts(
            @PathVariable UUID categoryId,
            @Valid @RequestBody ProductFilterDto filter) {
        
        log.info("Getting public category products - Category: {}", categoryId);
        
        // Set category ID for filtering
        filter.setCategoryId(categoryId);
        
        PaginationResponse<ProductListDto> products = productService.getAllProducts(filter);
        
        return ResponseEntity.ok(ApiResponse.success(
            String.format("Found %d products in category", products.getTotalElements()),
            products
        ));
    }
}