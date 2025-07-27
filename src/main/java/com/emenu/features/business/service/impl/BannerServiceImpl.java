package com.emenu.features.business.service.impl;

import com.emenu.exception.custom.NotFoundException;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.business.dto.filter.BannerFilterRequest;
import com.emenu.features.business.dto.request.BannerCreateRequest;
import com.emenu.features.business.dto.response.BannerResponse;
import com.emenu.features.business.dto.update.BannerUpdateRequest;
import com.emenu.features.business.mapper.BannerMapper;
import com.emenu.features.business.models.Banner;
import com.emenu.features.business.repository.BannerRepository;
import com.emenu.features.business.service.BannerService;
import com.emenu.features.business.specification.BannerSpecification;
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

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;
    private final BusinessRepository businessRepository;
    private final BannerMapper bannerMapper;
    private final SecurityUtils securityUtils;

    @Override
    public BannerResponse createBanner(BannerCreateRequest request) {
        log.info("Creating banner for business: {}", request.getBusinessId());

        // Validate business exists
        validateBusinessExists(request.getBusinessId());

        Banner banner = bannerMapper.toEntity(request);
        Banner savedBanner = bannerRepository.save(banner);

        log.info("Banner created successfully: {} for business: {}", savedBanner.getId(), request.getBusinessId());
        return bannerMapper.toResponse(savedBanner);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<BannerResponse> getAllBanners(BannerFilterRequest filter) {
        // Security: Platform users can see all banners, business users only their own
        applySecurity(filter);
        
        Specification<Banner> spec = BannerSpecification.buildSpecification(filter);
        
        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(
                pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        Page<Banner> bannerPage = bannerRepository.findAll(spec, pageable);
        return bannerMapper.toPaginationResponse(bannerPage);
    }

    @Override
    @Transactional(readOnly = true)
    public BannerResponse getBannerById(UUID id) {
        Banner banner = bannerRepository.findByIdWithBusiness(id)
                .orElseThrow(() -> new NotFoundException("Banner not found"));
        
        return bannerMapper.toResponse(banner);
    }

    @Override
    public BannerResponse updateBanner(UUID id, BannerUpdateRequest request) {
        Banner banner = bannerRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Banner not found"));


        bannerMapper.updateEntity(request, banner);
        Banner updatedBanner = bannerRepository.save(banner);

        log.info("Banner updated successfully: {}", id);
        return bannerMapper.toResponse(updatedBanner);
    }

    @Override
    public BannerResponse deleteBanner(UUID id) {
        Banner banner = bannerRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Banner not found"));

        banner.softDelete();
        banner = bannerRepository.save(banner);

        log.info("Banner deleted successfully: {}", id);
        return bannerMapper.toResponse(banner);
    }

    // Private helper methods
    private void validateBusinessExists(UUID businessId) {
        if (!businessRepository.existsById(businessId)) {
            throw new NotFoundException("Business not found");
        }
    }

    private void applySecurity(BannerFilterRequest filter) {
        User currentUser = securityUtils.getCurrentUser();
        
        // Business users can only see their own banners
        if (currentUser.isBusinessUser() && filter.getBusinessId() == null) {
            filter.setBusinessId(currentUser.getBusinessId());
        }
    }
}