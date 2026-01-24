package com.emenu.features.main.service.impl;

import com.emenu.exception.custom.NotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.models.User;
import com.emenu.features.main.dto.filter.BrandFilterRequest;
import com.emenu.features.main.dto.filter.BrandAllFilterRequest;
import com.emenu.features.main.dto.request.BrandCreateRequest;
import com.emenu.features.main.dto.response.BrandResponse;
import com.emenu.features.main.dto.update.BrandUpdateRequest;
import com.emenu.features.main.mapper.BrandMapper;
import com.emenu.features.main.models.Brand;
import com.emenu.features.main.repository.BrandRepository;
import com.emenu.features.main.service.BrandService;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;
    private final SecurityUtils securityUtils;
    private final com.emenu.shared.mapper.PaginationMapper paginationMapper;

    @Override
    public BrandResponse createBrand(BrandCreateRequest request) {
        log.info("Creating brand: {}", request.getName());

        User currentUser = securityUtils.getCurrentUser();
        if (currentUser.getBusinessId() == null) {
            throw new ValidationException("User is not associated with any business");
        }

        // Check if brand name already exists for this business
        if (brandRepository.existsByNameAndBusinessIdAndIsDeletedFalse(
                request.getName(), currentUser.getBusinessId())) {
            throw new ValidationException("Brand name already exists in your business");
        }

        Brand brand = brandMapper.toEntity(request);
        brand.setBusinessId(currentUser.getBusinessId());

        Brand savedBrand = brandRepository.save(brand);

        log.info("Brand created successfully: {} for business: {}",
                savedBrand.getName(), currentUser.getBusinessId());
        return brandMapper.toResponse(savedBrand);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<BrandResponse> getAllBrands(BrandFilterRequest filter) {
        Pageable pageable = PaginationUtils.createPageable(
                filter.getPageNo(), filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        Page<Brand> brandPage = brandRepository.findAllWithFilters(
                filter.getBusinessId(),
                filter.getStatus(),
                filter.getSearch(),
                pageable
        );
        return brandMapper.toPaginationResponse(brandPage, paginationMapper);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandResponse> getAllListBrands(BrandAllFilterRequest filter) {
        List<Brand> brandList = brandRepository.findAllWithFilters(
                filter.getBusinessId(),
                filter.getStatus(),
                filter.getSearch(),
                PaginationUtils.createSort(filter.getSortBy(), filter.getSortDirection())
        );
        return brandMapper.toResponseList(brandList);
    }

    @Override
    @Transactional(readOnly = true)
    public BrandResponse getBrandById(UUID id) {
        Brand brand = findBrandById(id);
        return brandMapper.toResponse(brand);
    }

    @Override
    public BrandResponse updateBrand(UUID id, BrandUpdateRequest request) {
        Brand brand = findBrandById(id);

        // Check if new name already exists (if name is being changed)
        if (request.getName() != null && !request.getName().equals(brand.getName())) {
            if (brandRepository.existsByNameAndBusinessIdAndIsDeletedFalse(
                    request.getName(), brand.getBusinessId())) {
                throw new ValidationException("Brand name already exists in your business");
            }
        }

        brandMapper.updateEntity(request, brand);
        Brand updatedBrand = brandRepository.save(brand);

        log.info("Brand updated successfully: {}", id);
        return brandMapper.toResponse(updatedBrand);
    }

    @Override
    public BrandResponse deleteBrand(UUID id) {
        Brand brand = findBrandById(id);

        // Check if brand is used by any products
        long productCount = brandRepository.countByBusinessId(brand.getId());
        if (productCount > 0) {
            throw new ValidationException("Cannot delete brand that is used by products");
        }

        brand.softDelete();
        brand = brandRepository.save(brand);

        log.info("Brand deleted successfully: {}", id);
        return brandMapper.toResponse(brand);
    }

    // Private helper methods
    private Brand findBrandById(UUID id) {
        return brandRepository.findByIdWithBusiness(id)
                .orElseThrow(() -> new NotFoundException("Brand not found"));
    }
}