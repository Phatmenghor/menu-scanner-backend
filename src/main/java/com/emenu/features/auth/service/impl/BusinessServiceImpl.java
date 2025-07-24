package com.emenu.features.auth.service.impl;

import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.dto.filter.BusinessFilterRequest;
import com.emenu.features.auth.dto.request.BusinessCreateRequest;
import com.emenu.features.auth.dto.response.BusinessResponse;
import com.emenu.features.auth.dto.update.BusinessUpdateRequest;
import com.emenu.features.auth.mapper.BusinessMapper;
import com.emenu.features.auth.mapper.UserMapper;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.auth.repository.SubscriptionRepository;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.auth.service.BusinessService;
import com.emenu.features.auth.specification.BusinessSpecification;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BusinessServiceImpl implements BusinessService {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final BusinessMapper businessMapper;
    private final UserMapper userMapper;

    @Override
    public BusinessResponse createBusiness(BusinessCreateRequest request) {
        log.info("Creating business: {}", request.getName());

        if (request.getEmail() != null && businessRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            throw new ValidationException("Business email already exists");
        }

        Business business = businessMapper.toEntity(request);
        Business savedBusiness = businessRepository.save(business);

        log.info("Business created successfully: {}", savedBusiness.getName());
        return businessMapper.toResponse(savedBusiness);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<BusinessResponse> getBusinesses(BusinessFilterRequest filter) {
        // ✅ Specification handles all filtering logic
        Specification<Business> spec = BusinessSpecification.buildSpecification(filter);
        
        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(
                pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        Page<Business> businessPage = businessRepository.findAll(spec, pageable);

        // ✅ Mapper handles pagination response conversion
        return businessMapper.toPaginationResponse(businessPage);
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessResponse getBusinessById(UUID id) {
        Business business = businessRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        BusinessResponse response = businessMapper.toResponse(business);

        // Add statistics
        response.setTotalStaff((int) userRepository.countByBusinessIdAndIsDeletedFalse(id));
        
        // Check subscription status
        var activeSubscription = subscriptionRepository.findCurrentActiveByBusinessId(id, LocalDateTime.now());
        response.setHasActiveSubscription(activeSubscription.isPresent());
        if (activeSubscription.isPresent()) {
            response.setCurrentSubscriptionPlan(activeSubscription.get().getPlan().getDisplayName());
            response.setDaysRemaining(activeSubscription.get().getDaysRemaining());
        }

        return response;
    }

    @Override
    public BusinessResponse updateBusiness(UUID id, BusinessUpdateRequest request) {
        Business business = businessRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        // ✅ Mapper handles entity updates
        businessMapper.updateEntity(request, business);
        Business updatedBusiness = businessRepository.save(business);

        log.info("Business updated successfully: {}", updatedBusiness.getName());
        return businessMapper.toResponse(updatedBusiness);
    }

    @Override
    public BusinessResponse deleteBusiness(UUID id) {
        Business business = businessRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        business.softDelete();
        business = businessRepository.save(business);

        BusinessResponse response = businessMapper.toResponse(business);

        response.setTotalStaff((int) userRepository.countByBusinessIdAndIsDeletedFalse(id));

        // Check subscription status
        var activeSubscription = subscriptionRepository.findCurrentActiveByBusinessId(id, LocalDateTime.now());
        response.setHasActiveSubscription(activeSubscription.isPresent());
        if (activeSubscription.isPresent()) {
            response.setCurrentSubscriptionPlan(activeSubscription.get().getPlan().getDisplayName());
            response.setDaysRemaining(activeSubscription.get().getDaysRemaining());
        }
        log.info("Business deleted successfully: {}", business.getName());

        return response;
    }
}