package com.emenu.features.product.controller;

import com.emenu.features.auth.models.User;
import com.emenu.features.product.dto.filter.ProductFilterRequest;
import com.emenu.features.product.dto.request.ProductCreateRequest;
import com.emenu.features.product.dto.response.*;
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
     * Reset promotion for product or specific size
     * - Without sizeId: resets entire product (all sizes)
     * - With sizeId: resets only that specific size
     */
    @PostMapping("/{productId}/promotion/reset")
    public ResponseEntity<ApiResponse<?>> resetPromotion(
            @PathVariable UUID productId,
            @RequestParam(required = false) UUID sizeId) {
        
        if (sizeId != null) {
            log.info("Resetting promotion for product {} size: {}", productId, sizeId);
            SizePromotionResetResponse result = productService.resetSizePromotion(productId, sizeId);
            return ResponseEntity.ok(ApiResponse.success("Size promotion reset successfully", result));
        } else {
            log.info("Resetting all promotions for product: {}", productId);
            ProductPromotionResetResponse result = productService.resetProductPromotion(productId);
            return ResponseEntity.ok(ApiResponse.success("Product promotion reset successfully", result));
        }
    }

    /**
     * Reset all promotions for current user's business
     */
    @PostMapping("/my-business/promotion/reset-all")
    public ResponseEntity<ApiResponse<BusinessPromotionResetResponse>> resetAllBusinessPromotions() {
        log.info("Resetting all promotions for current user's business");
        BusinessPromotionResetResponse result = productService.resetAllBusinessPromotions();
        return ResponseEntity.ok(ApiResponse.success("All business promotions reset successfully", result));
    }
}