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

    @PostMapping("/all")
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
        log.info("Get public product: {}", id);
        
        ProductDetailDto product = productService.getProductByIdPublic(id);
        
        return ResponseEntity.ok(ApiResponse.success("Product retrieved successfully", product));
    }
}