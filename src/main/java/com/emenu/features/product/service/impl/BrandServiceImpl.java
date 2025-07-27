package com.emenu.features.product.service.impl;

import com.emenu.enums.common.Status;
import com.emenu.exception.custom.NotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.models.User;
import com.emenu.features.product.dto.filter.BrandFilterRequest;
import com.emenu.features.product.dto.request.BrandCreateRequest;
import com.emenu.features.product.dto.response.BrandResponse;
import com.emenu.features.product.dto.update.BrandUpdateRequest;
import com.emenu.features.product.mapper.BrandMapper;
import com.emenu.features.product.models.Brand;
import com.emenu.features.product.repository.BrandRepository;
import com.emenu.features.product.service.BrandService;
import com.emenu.features.product.specification.BrandSpecification;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
        
        // Security: Business users can only see brands from their business
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser.isBusinessUser() && filter.getBusinessId() == null) {
            filter.setBusinessId(currentUser.getBusinessId());
        }
        
        Specification<Brand> spec = BrandSpecification.buildSpecification(filter);
        
        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(
                pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        Page<Brand> brandPage = brandRepository.findAll(spec, pageable);
        return brandMapper.toPaginationResponse(brandPage);
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

    @Override
    @Transactional(readOnly = true)
    public List<BrandResponse> getActiveBrandsByBusiness(UUID businessId) {
        List<Brand> brands = brandRepository.findActiveByBusinessId(businessId, Status.ACTIVE);
        return brandMapper.toResponseList(brands);
    }

    // Private helper methods
    private Brand findBrandById(UUID id) {
        return brandRepository.findByIdWithBusiness(id)
                .orElseThrow(() -> new NotFoundException("Brand not found"));
    }
}