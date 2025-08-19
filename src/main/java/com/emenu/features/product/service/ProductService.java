package com.emenu.features.product.service;

import com.emenu.features.product.dto.filter.ProductFilterDto;
import com.emenu.features.product.dto.request.ProductCreateDto;
import com.emenu.features.product.dto.response.ProductDetailDto;
import com.emenu.features.product.dto.response.ProductListDto;
import com.emenu.features.product.dto.update.ProductUpdateDto;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface ProductService {
    ProductDetailDto createProduct(ProductCreateDto request);
    PaginationResponse<ProductListDto> getAllProducts(ProductFilterDto filter);
    ProductDetailDto getProductById(UUID id);
    ProductDetailDto updateProduct(UUID id, ProductUpdateDto request);
    ProductDetailDto deleteProduct(UUID id);
    ProductDetailDto getProductByIdPublic(UUID id);
}