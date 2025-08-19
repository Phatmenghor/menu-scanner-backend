package com.emenu.features.product.controller;

import com.emenu.features.product.dto.filter.ProductFilterDto;
import com.emenu.features.product.dto.request.ProductCreateDto;
import com.emenu.features.product.dto.response.ProductDetailDto;
import com.emenu.features.product.dto.response.ProductListDto;
import com.emenu.features.product.dto.update.ProductUpdateDto;
import com.emenu.features.product.service.ProductService;
import com.emenu.shared.dto.ApiResponse;
import com.emenu.shared.dto.PaginationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    // ================================
    // Product CRUD Operations
    // ================================

    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<ProductListDto>>> searchProducts(
            @Valid @RequestBody ProductFilterDto filter) {
        
        log.info("Get all products - Page: {}, Size: {}", filter.getPageNo(), filter.getPageSize());
        
        PaginationResponse<ProductListDto> products = productService.getAllProducts(filter);
        
        return ResponseEntity.ok(ApiResponse.success(
            String.format("Found %d products", products.getTotalElements()),
            products
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDetailDto>> getProductById(@PathVariable UUID id) {
        log.info("Getting product by ID: {}", id);
        
        ProductDetailDto product = productService.getProductById(id);
        
        return ResponseEntity.ok(ApiResponse.success("Product retrieved successfully", product));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDetailDto>> createProduct(
            @Valid @RequestBody ProductCreateDto request) {
        
        log.info("Creating product: {}", request.getName());
        
        ProductDetailDto product = productService.createProduct(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully", product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDetailDto>> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductUpdateDto request) {
        
        log.info("Updating product: {} with data: {}", id, request.getName());
        
        ProductDetailDto product = productService.updateProduct(id, request);
        
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDetailDto>> deleteProduct(@PathVariable UUID id) {
        log.info("Deleting product: {}", id);
        
        ProductDetailDto product = productService.deleteProduct(id);
        
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", product));
    }
}