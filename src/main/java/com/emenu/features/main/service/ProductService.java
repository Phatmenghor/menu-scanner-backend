package com.emenu.features.main.service;

import com.emenu.features.main.dto.filter.ProductFilterDto;
import com.emenu.features.main.dto.request.ProductCreateDto;
import com.emenu.features.main.dto.response.ProductDetailDto;
import com.emenu.features.main.dto.response.ProductListDto;
import com.emenu.features.main.dto.update.ProductUpdateDto;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    ProductDetailDto createProduct(ProductCreateDto request);
    PaginationResponse<ProductListDto> getAllProductsAdmin(ProductFilterDto filter);
    PaginationResponse<ProductListDto> getAllProducts(ProductFilterDto filter);
    List<ProductListDto> getAllDataProducts(ProductFilterDto filter);
    ProductDetailDto getProductById(UUID id);
    ProductDetailDto updateProduct(UUID id, ProductUpdateDto request);
    ProductDetailDto deleteProduct(UUID id);
    ProductDetailDto getProductByIdPublic(UUID id);
}