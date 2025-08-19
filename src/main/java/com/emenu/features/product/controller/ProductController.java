package com.emenu.features.product.controller;

import com.emenu.features.product.dto.filter.ProductFilterDto;
import com.emenu.features.product.dto.request.ProductCreateDto;
import com.emenu.features.product.dto.response.FavoriteRemoveAllDto;
import com.emenu.features.product.dto.response.FavoriteToggleDto;
import com.emenu.features.product.dto.response.ProductDetailDto;
import com.emenu.features.product.dto.response.ProductListDto;
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
    // 🚀 FAST LISTING ENDPOINTS
    // ================================

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PaginationResponse<ProductListDto>>> searchProducts(
            @Valid @RequestBody ProductFilterDto filter) {
        
        log.info("Searching products - Page: {}, Size: {}, Search: '{}', Business: {}", 
                filter.getPageNo(), filter.getPageSize(), filter.getSearch(), filter.getBusinessId());
        
        PaginationResponse<ProductListDto> products = productService.getAllProducts(filter);
        
        return ResponseEntity.ok(ApiResponse.success(
            String.format("Found %d products (Page %d of %d)", 
                products.getTotalElements(), products.getPageNo(), products.getTotalPages()),
            products
        ));
    }

    @PostMapping("/my-business")
    public ResponseEntity<ApiResponse<PaginationResponse<ProductListDto>>> getMyBusinessProducts(
            @Valid @RequestBody ProductFilterDto filter) {
        
        log.info("Getting my business products - Page: {}, Size: {}", 
                filter.getPageNo(), filter.getPageSize());
        
        PaginationResponse<ProductListDto> products = productService.getAllProducts(filter);
        
        return ResponseEntity.ok(ApiResponse.success(
            String.format("Retrieved %d business products", products.getTotalElements()),
            products
        ));
    }

    // ================================
    // 🚀 SINGLE PRODUCT ENDPOINTS
    // ================================

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDetailDto>> getProductById(@PathVariable UUID id) {
        log.info("Getting product by ID: {}", id);
        
        ProductDetailDto product = productService.getProductById(id);
        
        return ResponseEntity.ok(ApiResponse.success("Product retrieved successfully", product));
    }

    @GetMapping("/{id}/public")
    public ResponseEntity<ApiResponse<ProductDetailDto>> getProductByIdPublic(@PathVariable UUID id) {
        log.info("Getting product by ID (public): {}", id);
        
        ProductDetailDto product = productService.getProductByIdPublic(id);
        
        return ResponseEntity.ok(ApiResponse.success("Product retrieved successfully", product));
    }

    // ================================
    // 🚀 CRUD OPERATIONS
    // ================================

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
            @Valid @RequestBody ProductCreateDto request) {
        
        log.info("Updating product: {}", id);
        
        ProductDetailDto product = productService.updateProduct(id, request);
        
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDetailDto>> deleteProduct(@PathVariable UUID id) {
        log.info("Deleting product: {}", id);
        
        ProductDetailDto product = productService.deleteProduct(id);
        
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", product));
    }

    // ================================
    // 🚀 FAVORITES ENDPOINTS
    // ================================

    @PostMapping("/{id}/favorite/toggle")
    public ResponseEntity<ApiResponse<FavoriteToggleDto>> toggleFavorite(@PathVariable UUID id) {
        log.info("Toggling favorite for product: {}", id);
        
        FavoriteToggleDto result = productService.toggleFavorite(id);
        
        return ResponseEntity.ok(ApiResponse.success(result.getMessage(), result));
    }

    @PostMapping("/favorites")
    public ResponseEntity<ApiResponse<PaginationResponse<ProductListDto>>> getUserFavorites(
            @Valid @RequestBody ProductFilterDto filter) {
        
        log.info("Getting user favorites - Page: {}, Size: {}", 
                filter.getPageNo(), filter.getPageSize());
        
        PaginationResponse<ProductListDto> favorites = productService.getUserFavorites(filter);
        
        return ResponseEntity.ok(ApiResponse.success(
            String.format("Retrieved %d favorite products", favorites.getTotalElements()),
            favorites
        ));
    }

    @DeleteMapping("/favorites/all")
    public ResponseEntity<ApiResponse<FavoriteRemoveAllDto>> removeAllFavorites() {
        log.info("Removing all favorites for current user");
        
        FavoriteRemoveAllDto result = productService.removeAllFavorites();
        
        return ResponseEntity.ok(ApiResponse.success(result.getMessage(), result));
    }
}