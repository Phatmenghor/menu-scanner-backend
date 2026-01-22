package com.emenu.features.auth.service.impl;

import com.emenu.enums.user.BusinessStatus;
import com.emenu.enums.user.UserType;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.dto.filter.BusinessFilterRequest;
import com.emenu.features.auth.dto.request.BusinessCreateRequest;
import com.emenu.features.auth.dto.response.BusinessResponse;
import com.emenu.features.auth.mapper.BusinessMapper;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.auth.service.BusinessService;
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
public class BusinessServiceImpl implements BusinessService {

    private final BusinessRepository businessRepository;
    private final BusinessMapper businessMapper;
    private final com.emenu.shared.mapper.PaginationMapper paginationMapper;

    /**
     * Creates a new business
     */
    @Override
    public BusinessResponse createBusiness(BusinessCreateRequest request) {
        log.info("Creating business: {}", request.getName());

        if (businessRepository.existsByNameAndIsDeletedFalse(request.getName())) {
            throw new ValidationException("Business name already exists");
        }

        Business business = businessMapper.toEntity(request);
        Business savedBusiness = businessRepository.save(business);

        log.info("Business created: {}", savedBusiness.getName());
        return businessMapper.toResponse(savedBusiness);
    }

    /**
     * Retrieves all businesses with filtering and pagination support
     */
    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<BusinessResponse> getAllBusinesses(BusinessFilterRequest request) {
        log.info("Getting all businesses with filters");

        Pageable pageable = PaginationUtils.createPageable(
                request.getPageNo(),
                request.getPageSize(), 
                request.getSortBy(), 
                request.getSortDirection()
        );

        List<BusinessStatus> businessStatuses = (request.getStatus() != null && !request.getStatus().isEmpty())
                ? request.getStatus() : null;

        Page<Business> businessPage = businessRepository.searchBusinesses(
                businessStatuses,
                request.getHasActiveSubscription(),
                request.getSearch(),
                pageable
        );

        return businessMapper.toPaginationResponse(businessPage, paginationMapper);
    }

    /**
     * Retrieves a business by ID
     */
    @Override
    @Transactional(readOnly = true)
    public BusinessResponse getBusinessById(UUID businessId) {
        Business business = businessRepository.findByIdAndIsDeletedFalse(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found"));
        return businessMapper.toResponse(business);
    }

    /**
     * Updates an existing business
     */
    @Override
    public BusinessResponse updateBusiness(UUID businessId, BusinessCreateRequest request) {
        log.info("Updating business: {}", businessId);

        Business business = businessRepository.findByIdAndIsDeletedFalse(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        business.setName(request.getName());
        business.setEmail(request.getEmail());
        business.setPhone(request.getPhone());
        business.setAddress(request.getAddress());
        business.setDescription(request.getDescription());

        Business updatedBusiness = businessRepository.save(business);

        log.info("Business updated: {}", updatedBusiness.getName());
        return businessMapper.toResponse(updatedBusiness);
    }

    /**
     * Soft deletes a business
     */
    @Override
    public void deleteBusiness(UUID businessId) {
        Business business = businessRepository.findByIdAndIsDeletedFalse(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        business.softDelete();
        businessRepository.save(business);
        log.info("Business deleted: {}", business.getName());
    }
}
