package com.emenu.features.product.service.impl;

import com.emenu.exception.custom.NotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.models.User;
import com.emenu.features.product.dto.filter.ProductFilterDto;
import com.emenu.features.product.dto.request.ProductCreateDto;
import com.emenu.features.product.dto.request.ProductImageCreateDto;
import com.emenu.features.product.dto.request.ProductSizeCreateDto;
import com.emenu.features.product.dto.response.ProductDetailDto;
import com.emenu.features.product.dto.response.ProductListDto;
import com.emenu.features.product.dto.update.ProductImageUpdateDto;
import com.emenu.features.product.dto.update.ProductSizeUpdateDto;
import com.emenu.features.product.dto.update.ProductUpdateDto;
import com.emenu.features.product.mapper.ProductImageMapper;
import com.emenu.features.product.mapper.ProductMapper;
import com.emenu.features.product.mapper.ProductSizeMapper;
import com.emenu.features.product.models.Product;
import com.emenu.features.product.models.ProductImage;
import com.emenu.features.product.models.ProductSize;
import com.emenu.features.product.repository.ProductImageRepository;
import com.emenu.features.product.repository.ProductRepository;
import com.emenu.features.product.repository.ProductSizeRepository;
import com.emenu.features.product.service.ProductService;
import com.emenu.features.product.specification.ProductSpecifications;
import com.emenu.features.product.utils.ProductFavoriteQueryHelper;
import com.emenu.features.product.utils.ProductUtils;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductSizeRepository productSizeRepository;
    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final ProductSizeMapper productSizeMapper;
    private final PaginationMapper paginationMapper;
    private final SecurityUtils securityUtils;
    private final ProductUtils productUtils;
    private final ProductFavoriteQueryHelper favoriteQueryHelper;

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<ProductListDto> getAllProducts(ProductFilterDto filter) {
        Optional<User> currentUser = securityUtils.getCurrentUserOptional();
        if (currentUser.isPresent() && currentUser.get().isBusinessUser() && filter.getBusinessId() == null) {
            filter.setBusinessId(currentUser.get().getBusinessId());
        }

        Pageable pageable = PaginationUtils.createPageable(
                filter.getPageNo() != null ? filter.getPageNo() - 1 : null,
                filter.getPageSize(),
                filter.getSortBy(),
                filter.getSortDirection()
        );

        Specification<Product> spec = ProductSpecifications.withFilter(filter);
        Page<Product> productPage = productRepository.findAll(spec, pageable);

        if (productPage.getContent().isEmpty()) {
            return paginationMapper.toPaginationResponse(productPage, Collections.emptyList());
        }

        List<ProductListDto> dtoList = productMapper.toListDtos(productPage.getContent());

        if (currentUser.isPresent()) {
            List<UUID> productIds = productPage.getContent().stream()
                    .map(Product::getId)
                    .toList();
            
            List<UUID> favoriteIds = favoriteQueryHelper.getFavoriteProductIds(
                    currentUser.get().getId(), 
                    productIds
            );
            Set<UUID> favoriteSet = new HashSet<>(favoriteIds);

            dtoList.forEach(dto -> dto.setIsFavorited(favoriteSet.contains(dto.getId())));
        } else {
            dtoList.forEach(dto -> dto.setIsFavorited(false));
        }

        return paginationMapper.toPaginationResponse(productPage, dtoList);
    }


    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<ProductListDto> getAllProductsAdmin(ProductFilterDto filter) {
        Optional<User> currentUser = securityUtils.getCurrentUserOptional();
        if (currentUser.isPresent() && currentUser.get().isBusinessUser() && filter.getBusinessId() == null) {
            filter.setBusinessId(currentUser.get().getBusinessId());
        }

        Pageable pageable = PaginationUtils.createPageable(
                filter.getPageNo() != null ? filter.getPageNo() - 1 : null,
                filter.getPageSize(),
                filter.getSortBy(),
                filter.getSortDirection()
        );

        Specification<Product> spec = ProductSpecifications.withFilter(filter);
        Page<Product> productPage = productRepository.findAll(spec, pageable);

        List<ProductListDto> dtoList = productMapper.toListDtos(productPage.getContent());

        return paginationMapper.toPaginationResponse(productPage, dtoList);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailDto getProductById(UUID id) {
        Product product = productRepository.findByIdWithAllDetails(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));

        Optional<User> currentUser = securityUtils.getCurrentUserOptional();
        if (currentUser.isPresent() && currentUser.get().isBusinessUser()) {
            validateBusinessAccess(product, currentUser.get());
        }

        ProductDetailDto dto = productMapper.toDetailDto(product);

        if (currentUser.isPresent()) {
            boolean isFavorited = favoriteQueryHelper.isFavorited(currentUser.get().getId(), product.getId());
            dto.setIsFavorited(isFavorited);
        }

        return dto;
    }

    @Override
    @Transactional
    public ProductDetailDto getProductByIdPublic(UUID id) {
        Product product = productRepository.findByIdWithAllDetails(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));

        productRepository.incrementViewCount(id);

        ProductDetailDto dto = productMapper.toDetailDto(product);

        Optional<User> currentUser = securityUtils.getCurrentUserOptional();
        if (currentUser.isPresent()) {
            boolean isFavorited = favoriteQueryHelper.isFavorited(currentUser.get().getId(), product.getId());
            dto.setIsFavorited(isFavorited);
        }

        return dto;
    }

    @Override
    public ProductDetailDto createProduct(ProductCreateDto request) {
        User currentUser = securityUtils.getCurrentUser();
        validateUserBusinessAssociation(currentUser);

        Product product = productMapper.toEntity(request);
        product.setBusinessId(currentUser.getBusinessId());
        product.setViewCount(0L);
        product.setFavoriteCount(0L);

        product.initializeDisplayFields();

        Product savedProduct = productRepository.save(product);

        handleProductImages(savedProduct, request.getImages());
        
        if (request.getSizes() != null && !request.getSizes().isEmpty()) {
            handleProductSizes(savedProduct, request.getSizes());
            
            List<ProductSize> sizes = productSizeRepository.findByProductId(savedProduct.getId());
            savedProduct.setSizes(sizes);
            savedProduct.syncDisplayFieldsFromSizes();
            savedProduct = productRepository.save(savedProduct);
        }

        return getProductById(savedProduct.getId());
    }

    @Override
    public ProductDetailDto updateProduct(UUID id, ProductUpdateDto request) {
        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));

        User currentUser = securityUtils.getCurrentUser();
        validateBusinessOwnership(product, currentUser);

        productMapper.updateEntity(request, product);

        if (!product.getHasSizes()) {
            product.initializeDisplayFields();
        }

        Product updatedProduct = productRepository.save(product);

        updateProductImages(updatedProduct, request.getImages());
        
        boolean sizesChanged = updateProductSizes(updatedProduct, request.getSizes());
        
        if (sizesChanged) {
            List<ProductSize> sizes = productSizeRepository.findByProductId(updatedProduct.getId());
            updatedProduct.setSizes(sizes);
            updatedProduct.syncDisplayFieldsFromSizes();
            updatedProduct = productRepository.save(updatedProduct);
        }

        return getProductById(updatedProduct.getId());
    }

    @Override
    public ProductDetailDto deleteProduct(UUID id) {
        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));

        User currentUser = securityUtils.getCurrentUser();
        validateBusinessOwnership(product, currentUser);

        product.softDelete();
        Product deletedProduct = productRepository.save(product);

        return productMapper.toDetailDto(deletedProduct);
    }

    private void handleProductImages(Product product, List<ProductImageCreateDto> imageDtos) {
        if (imageDtos == null || imageDtos.isEmpty()) return;

        List<ProductImage> images = imageDtos.stream()
                .filter(imageDto -> productUtils.isValidImageUrl(imageDto.getImageUrl()))
                .map(imageDto -> {
                    ProductImage image = productImageMapper.toEntity(imageDto);
                    image.setProductId(product.getId());
                    return image;
                })
                .toList();

        if (!images.isEmpty()) {
            productImageRepository.saveAll(images);
        }
    }

    private void handleProductSizes(Product product, List<ProductSizeCreateDto> sizeDtos) {
        if (sizeDtos == null || sizeDtos.isEmpty()) return;

        List<ProductSize> sizes = sizeDtos.stream()
                .map(sizeDto -> {
                    ProductSize size = productSizeMapper.toEntity(sizeDto);
                    size.setProductId(product.getId());
                    return size;
                })
                .toList();

        productSizeRepository.saveAll(sizes);
    }

    private void updateProductImages(Product product, List<ProductImageUpdateDto> imageDtos) {
        if (imageDtos == null || imageDtos.isEmpty()) return;

        List<ProductImage> existingImages = productImageRepository.findByProductId(product.getId());

        List<UUID> idsToDelete = productImageMapper.getIdsToDelete(imageDtos);
        if (!idsToDelete.isEmpty()) {
            existingImages.stream()
                    .filter(img -> idsToDelete.contains(img.getId()))
                    .forEach(img -> {
                        img.softDelete();
                        productImageRepository.save(img);
                    });
        }

        List<ProductImageUpdateDto> toUpdate = productImageMapper.getExistingToUpdate(imageDtos);
        for (ProductImageUpdateDto updateDto : toUpdate) {
            existingImages.stream()
                    .filter(img -> img.getId().equals(updateDto.getId()))
                    .findFirst()
                    .ifPresent(existingImage -> {
                        productImageMapper.updateEntity(updateDto, existingImage);
                        productImageRepository.save(existingImage);
                    });
        }

        List<ProductImage> newImages = productImageMapper.toEntitiesFromUpdate(imageDtos);
        newImages.forEach(img -> img.setProductId(product.getId()));
        if (!newImages.isEmpty()) {
            productImageRepository.saveAll(newImages);
        }
    }

    private boolean updateProductSizes(Product product, List<ProductSizeUpdateDto> sizeDtos) {
        if (sizeDtos == null || sizeDtos.isEmpty()) return false;

        boolean changed = false;
        List<ProductSize> existingSizes = productSizeRepository.findByProductId(product.getId());

        List<UUID> idsToDelete = productSizeMapper.getIdsToDelete(sizeDtos);
        if (!idsToDelete.isEmpty()) {
            existingSizes.stream()
                    .filter(size -> idsToDelete.contains(size.getId()))
                    .forEach(size -> {
                        size.softDelete();
                        productSizeRepository.save(size);
                    });
            changed = true;
        }

        List<ProductSizeUpdateDto> toUpdate = productSizeMapper.getExistingToUpdate(sizeDtos);
        if (!toUpdate.isEmpty()) {
            for (ProductSizeUpdateDto updateDto : toUpdate) {
                existingSizes.stream()
                        .filter(size -> size.getId().equals(updateDto.getId()))
                        .findFirst()
                        .ifPresent(existingSize -> {
                            productSizeMapper.updateEntity(updateDto, existingSize);
                            productSizeRepository.save(existingSize);
                        });
            }
            changed = true;
        }

        List<ProductSize> newSizes = productSizeMapper.toEntitiesFromUpdate(sizeDtos);
        if (!newSizes.isEmpty()) {
            newSizes.forEach(size -> size.setProductId(product.getId()));
            productSizeRepository.saveAll(newSizes);
            changed = true;
        }

        return changed;
    }

    private void validateUserBusinessAssociation(User user) {
        if (user.getBusinessId() == null) {
            throw new ValidationException("User is not associated with any business");
        }
    }

    private void validateBusinessOwnership(Product product, User user) {
        if (!product.getBusinessId().equals(user.getBusinessId())) {
            throw new ValidationException("You can only modify products from your own business");
        }
    }

    private void validateBusinessAccess(Product product, User user) {
        if (user.isBusinessUser() && !product.getBusinessId().equals(user.getBusinessId())) {
            throw new ValidationException("Access denied to product from different business");
        }
    }
}