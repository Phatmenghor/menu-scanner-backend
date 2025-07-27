package com.emenu.features.product.service;

import com.emenu.features.product.dto.filter.ProductFilterRequest;
import com.emenu.features.product.dto.request.ProductCreateRequest;
import com.emenu.features.product.dto.response.ProductResponse;
import com.emenu.features.product.dto.response.ProductSummaryResponse;
import com.emenu.features.product.dto.update.ProductUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    
    // CRUD Operations
    ProductResponse createProduct(ProductCreateRequest request);
    PaginationResponse<ProductResponse> getAllProducts(ProductFilterRequest filter);
    PaginationResponse<ProductSummaryResponse> getAllProductsSummary(ProductFilterRequest filter);
    ProductResponse getProductById(UUID id);
    ProductResponse updateProduct(UUID id, ProductUpdateRequest request);
    ProductResponse deleteProduct(UUID id);
    
    // Public Operations (for customer-facing features)
    ProductResponse getProductByIdPublic(UUID id);
    void incrementProductView(UUID id);
    
    // Favorite Operations
    void addToFavorites(UUID productId);
    void removeFromFavorites(UUID productId);
    PaginationResponse<ProductResponse> getUserFavorites(ProductFilterRequest filter);
    
    // Additional Operations
    List<ProductResponse> getProductsByCategory(UUID categoryId);
    List<ProductResponse> getProductsByBrand(UUID brandId);
}