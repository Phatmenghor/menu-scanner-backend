package com.emenu.features.product.controller;

import com.emenu.features.auth.models.User;
import com.emenu.features.product.dto.filter.ProductFilterRequest;
import com.emenu.features.product.dto.request.ProductCreateRequest;
import com.emenu.features.product.dto.response.ProductResponse;
import com.emenu.features.product.dto.update.ProductUpdateRequest;
import com.emenu.features.product.service.ProductService;
import com.emenu.security.SecurityUtils;
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
    private final SecurityUtils securityUtils;

    /**
     * Create new product (uses current user's business from token)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        log.info("Creating product: {}", request.getName());
        ProductResponse product = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully", product));
    }

    /**
     * Get all products with filtering (full details)
     */
    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<ProductResponse>>> getAllProducts(@Valid @RequestBody ProductFilterRequest filter) {
        log.info("Getting all products for current user's business");
        PaginationResponse<ProductResponse> products = productService.getAllProducts(filter);
        return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", products));
    }

    /**
     * Get my business products
     */
    @PostMapping("/my-business/all")
    public ResponseEntity<ApiResponse<PaginationResponse<ProductResponse>>> getMyBusinessProducts(@Valid @RequestBody ProductFilterRequest filter) {
        log.info("Getting products for current user's business");
        User currentUser = securityUtils.getCurrentUser();
        filter.setBusinessId(currentUser.getBusinessId());
        PaginationResponse<ProductResponse> products = productService.getAllProducts(filter);
        return ResponseEntity.ok(ApiResponse.success("Business products retrieved successfully", products));
    }

    /**
     * Get product by ID (admin view)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable UUID id) {
        log.info("Getting product by ID: {}", id);
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success("Product retrieved successfully", product));
    }

    /**
     * Get product by ID (public view - increments view count)
     */
    @GetMapping("/{id}/public")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductByIdPublic(@PathVariable UUID id) {
        log.info("Getting product by ID (public): {}", id);
        ProductResponse product = productService.getProductByIdPublic(id);
        return ResponseEntity.ok(ApiResponse.success("Product retrieved successfully", product));
    }

    /**
     * Update product
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductUpdateRequest request) {
        log.info("Updating product: {}", id);
        ProductResponse product = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", product));
    }

    /**
     * Delete product
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> deleteProduct(@PathVariable UUID id) {
        log.info("Deleting product: {}", id);
        ProductResponse product = productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", product));
    }

    /**
     * Add product to favorites
     */
    @PostMapping("/{id}/favorite")
    public ResponseEntity<ApiResponse<Void>> addToFavorites(@PathVariable UUID id) {
        log.info("Adding product to favorites: {}", id);
        productService.addToFavorites(id);
        return ResponseEntity.ok(ApiResponse.success("Product added to favorites successfully", null));
    }

    /**
     * Remove product from favorites
     */
    @DeleteMapping("/{id}/favorite")
    public ResponseEntity<ApiResponse<Void>> removeFromFavorites(@PathVariable UUID id) {
        log.info("Removing product from favorites: {}", id);
        productService.removeFromFavorites(id);
        return ResponseEntity.ok(ApiResponse.success("Product removed from favorites successfully", null));
    }

    /**
     * Get user's favorite products
     */
    @PostMapping("/favorites")
    public ResponseEntity<ApiResponse<PaginationResponse<ProductResponse>>> getUserFavorites(@Valid @RequestBody ProductFilterRequest filter) {
        log.info("Getting user's favorite products");
        PaginationResponse<ProductResponse> favorites = productService.getUserFavorites(filter);
        return ResponseEntity.ok(ApiResponse.success("Favorite products retrieved successfully", favorites));
    }
}