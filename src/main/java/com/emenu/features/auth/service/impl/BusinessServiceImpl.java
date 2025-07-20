package com.emenu.features.auth.service.impl;

import com.emenu.enums.BusinessStatus;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.dto.filter.BusinessFilterRequest;
import com.emenu.features.auth.dto.filter.UserFilterRequest;
import com.emenu.features.auth.dto.request.BusinessCreateRequest;
import com.emenu.features.auth.dto.response.BusinessResponse;
import com.emenu.features.auth.dto.response.DashboardStatsResponse;
import com.emenu.features.auth.dto.response.UserResponse;
import com.emenu.features.auth.dto.update.BusinessUpdateRequest;
import com.emenu.features.auth.mapper.BusinessMapper;
import com.emenu.features.auth.mapper.UserMapper;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.models.Subscription;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.auth.repository.SubscriptionRepository;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.auth.service.BusinessService;
import com.emenu.features.auth.specification.BusinessSpecification;
import com.emenu.features.auth.specification.UserSpecification;
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

        // ✅ Mapper handles entity creation
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
    public void deleteBusiness(UUID id) {
        Business business = businessRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        business.softDelete();
        businessRepository.save(business);
        log.info("Business deleted successfully: {}", business.getName());
    }

    @Override
    public void activateBusiness(UUID id) {
        Business business = businessRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        business.setStatus(BusinessStatus.ACTIVE);
        businessRepository.save(business);
        log.info("Business activated successfully: {}", business.getName());
    }

    @Override
    public void suspendBusiness(UUID id) {
        Business business = businessRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        business.setStatus(BusinessStatus.SUSPENDED);
        businessRepository.save(business);
        log.info("Business suspended successfully: {}", business.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getBusinessStats(UUID businessId) {
        DashboardStatsResponse stats = new DashboardStatsResponse();

        // Basic business stats
        stats.setTotalStaff((int) userRepository.countByBusinessIdAndIsDeletedFalse(businessId));
        stats.setActiveStaff((int) userRepository.countByBusinessIdAndIsDeletedFalse(businessId)); // Simplified

        // Subscription stats
        var activeSubscription = subscriptionRepository.findCurrentActiveByBusinessId(businessId, LocalDateTime.now());
        if (activeSubscription.isPresent()) {
            Subscription subscription = activeSubscription.get();
            stats.setCurrentPlan(subscription.getPlan().getDisplayName());
            stats.setDaysRemaining(subscription.getDaysRemaining());
            
            int currentStaff = (int) userRepository.countByBusinessIdAndIsDeletedFalse(businessId);
            stats.setCanAddStaff(subscription.canAddStaff(currentStaff));
            stats.setCanAddMenuItem(subscription.canAddMenuItem(0)); // Pass actual count
            stats.setCanAddTable(subscription.canAddTable(0)); // Pass actual count
            
            stats.setStaffUsage(currentStaff);
            stats.setStaffLimit(subscription.getPlan().getMaxStaff());
        }

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<UserResponse> getBusinessStaff(UUID businessId, UserFilterRequest filter) {
        // Set business filter
        filter.setBusinessId(businessId);
        
        // ✅ Specification handles all filtering logic
        Specification<User> spec = UserSpecification.buildSpecification(filter);
        
        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(
                pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        Page<User> staffPage = userRepository.findAll(spec, pageable);

        // ✅ Mapper handles pagination response conversion
        return userMapper.toPaginationResponse(staffPage);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canAddMoreStaff(UUID businessId) {
        var activeSubscription = subscriptionRepository.findCurrentActiveByBusinessId(businessId, LocalDateTime.now());
        if (activeSubscription.isEmpty()) {
            return false; // No active subscription
        }

        int currentStaffCount = (int) userRepository.countByBusinessIdAndIsDeletedFalse(businessId);
        return activeSubscription.get().canAddStaff(currentStaffCount);
    }
}