package com.emenu.features.auth.service.impl;

import com.emenu.features.auth.dto.filter.BusinessFilterRequest;
import com.emenu.features.auth.dto.request.BusinessCreateRequest;
import com.emenu.features.auth.dto.response.BusinessResponse;
import com.emenu.features.auth.dto.update.BusinessUpdateRequest;
import com.emenu.features.auth.mapper.BusinessMapper;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.auth.service.BusinessService;
import com.emenu.features.auth.specification.BusinessSpecification;
import com.emenu.features.subscription.repository.SubscriptionRepository;
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

    @Override
    public BusinessResponse createBusiness(BusinessCreateRequest request) {
        log.info("Creating business: {}", request.getName());

        Business business = businessMapper.toEntity(request);
        Business savedBusiness = businessRepository.save(business);

        log.info("Business created successfully: {}", savedBusiness.getName());
        return businessMapper.toResponse(savedBusiness);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<BusinessResponse> getBusinesses(BusinessFilterRequest filter) {
        Specification<Business> spec = BusinessSpecification.buildSpecification(filter);
        
        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(
                pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        Page<Business> businessPage = businessRepository.findAll(spec, pageable);
        return businessMapper.toPaginationResponse(businessPage);
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessResponse getBusinessById(UUID id) {
        log.info("Getting business by ID: {}", id);
        
        Business business = businessRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        BusinessResponse response = businessMapper.toResponse(business);

        enrichBusinessResponse(response, id);

        log.info("Successfully retrieved business: {} with subscription status: {}", 
                business.getName(), response.getIsSubscriptionActive());
        
        return response;
    }

    @Override
    public BusinessResponse updateBusiness(UUID id, BusinessUpdateRequest request) {
        log.info("Updating business: {}", id);
        
        Business business = businessRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        businessMapper.updateEntity(request, business);
        Business updatedBusiness = businessRepository.save(business);

        BusinessResponse response = businessMapper.toResponse(updatedBusiness);
        enrichBusinessResponse(response, id);

        log.info("Business updated successfully: {}", updatedBusiness.getName());
        return response;
    }

    @Override
    public BusinessResponse deleteBusiness(UUID id) {
        log.info("Deleting business: {}", id);
        
        Business business = businessRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        business.softDelete();
        business = businessRepository.save(business);

        BusinessResponse response = businessMapper.toResponse(business);
        enrichBusinessResponse(response, id);
        
        log.info("Business deleted successfully: {}", business.getName());
        return response;
    }

    /**
     * ✅ NEW: Helper method to enrich business response with additional statistics
     * This method fetches additional data separately to avoid collection fetching issues
     */
    private void enrichBusinessResponse(BusinessResponse response, UUID businessId) {
        try {
            // Get user count
            long userCount = userRepository.countByBusinessIdAndIsDeletedFalse(businessId);
            response.setTotalStaff((int) userCount);
            
            // Get active subscription details
            var activeSubscription = subscriptionRepository.findCurrentActiveByBusinessId(businessId, LocalDateTime.now());
            boolean hasActiveSubscription = activeSubscription.isPresent();
            response.setIsSubscriptionActive(hasActiveSubscription);
            
            if (hasActiveSubscription) {
                var subscription = activeSubscription.get();
                if (subscription.getPlan() != null) {
                    response.setCurrentSubscriptionPlan(subscription.getPlan().getName());
                } else {
                    response.setCurrentSubscriptionPlan("Unknown Plan");
                }
                response.setDaysRemaining(subscription.getDaysRemaining());
                response.setSubscriptionStartDate(subscription.getStartDate());
                response.setSubscriptionEndDate(subscription.getEndDate());
                response.setIsExpiringSoon(subscription.getDaysRemaining() <= 7);
            } else {
                response.setCurrentSubscriptionPlan("No Active Plan");
                response.setDaysRemaining(0L);
                response.setIsExpiringSoon(false);
            }
            
            log.debug("Enriched business response - Users: {}, HasSubscription: {}, Plan: {}", 
                    userCount, hasActiveSubscription, response.getCurrentSubscriptionPlan());
                    
        } catch (Exception e) {
            log.warn("Failed to enrich business response for ID: {} - {}", businessId, e.getMessage());
            
            // Set safe defaults
            response.setTotalStaff(0);
            response.setIsSubscriptionActive(false);
            response.setCurrentSubscriptionPlan("No Active Plan");
            response.setDaysRemaining(0L);
            response.setIsExpiringSoon(false);
        }
    }
}