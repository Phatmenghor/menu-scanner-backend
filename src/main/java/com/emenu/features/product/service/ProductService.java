package com.emenu.features.product.service;

import com.emenu.features.product.dto.filter.ProductFilterDto;
import com.emenu.features.product.dto.request.ProductCreateDto;
import com.emenu.features.product.dto.response.ProductDetailDto;
import com.emenu.features.product.dto.response.ProductListDto;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface ProductService {

    // CRUD Operations
    ProductDetailDto createProduct(ProductCreateDto request);

    PaginationResponse<ProductListDto> getAllProducts(ProductFilterDto filter);

    ProductDetailDto getProductById(UUID id);

    ProductDetailDto updateProduct(UUID id, ProductCreateDto request);

    ProductDetailDto deleteProduct(UUID id);

    // Public Operations
    ProductDetailDto getProductByIdPublic(UUID id);
}
