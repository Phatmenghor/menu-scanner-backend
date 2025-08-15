package com.emenu.features.product.controller;

import com.emenu.features.product.dto.filter.ProductFilterRequest;
import com.emenu.features.product.dto.response.ProductResponse;
import com.emenu.features.product.service.ProductService;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.ApiResponse;
import com.emenu.shared.dto.PaginationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public/product")
@RequiredArgsConstructor
@Slf4j
public class PublicProductController {

    private final ProductService productService;
    private final SecurityUtils securityUtils;

    /**
     * Get my business products
     */
    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<ProductResponse>>> getMyBusinessProducts(@Valid @RequestBody ProductFilterRequest filter) {
        log.info("Getting products for current user's business");
        PaginationResponse<ProductResponse> products = productService.getAllProducts(filter);
        return ResponseEntity.ok(ApiResponse.success("Business products retrieved successfully", products));
    }

    /**
     * Get product by ID (public view - increments view count)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductByIdPublic(@PathVariable UUID id) {
        log.info("Getting product by ID (public): {}", id);
        ProductResponse product = productService.getProductByIdPublic(id);
        return ResponseEntity.ok(ApiResponse.success("Product retrieved successfully", product));
    }
}
