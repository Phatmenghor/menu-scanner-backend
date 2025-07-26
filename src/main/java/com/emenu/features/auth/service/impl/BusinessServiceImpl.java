package com.emenu.features.auth.service.impl;

import com.emenu.exception.custom.ValidationException;
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
import com.emenu.features.subdomain.service.SubdomainService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BusinessServiceImpl implements BusinessService {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubdomainService subdomainService;
    private final BusinessMapper businessMapper;

    @Override
    public BusinessResponse createBusiness(BusinessCreateRequest request) {
        log.info("Creating business: {}", request.getName());

        // ✅ ENHANCED: Early validation with specific error messages
        validateBusinessCreation(request);

        Business business = businessMapper.toEntity(request);
        Business savedBusiness = businessRepository.save(business);

        // ✅ FIXED: Auto-create subdomain for the business - ALWAYS create one
        try {
            String preferredSubdomain = generateSubdomainFromBusinessName(request.getName());
            subdomainService.createSubdomainForBusiness(savedBusiness.getId(), preferredSubdomain);
            log.info("Subdomain created automatically for business: {} -> {}", savedBusiness.getName(), preferredSubdomain);
        } catch (Exception e) {
            log.error("Failed to create subdomain for business: {} - Error: {}", savedBusiness.getName(), e.getMessage());
            // Still proceed with business creation, but log the error
        }

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
        // ✅ FIXED: Use method that loads all relationships
        Business business = businessRepository.findByIdWithAllRelationships(id)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        BusinessResponse response = businessMapper.toResponse(business);

        // ✅ FIXED: Add statistics with proper loading
        response.setTotalStaff((int) userRepository.countByBusinessIdAndIsDeletedFalse(id));
        
        // ✅ FIXED: Check subscription status properly
        var activeSubscription = subscriptionRepository.findCurrentActiveByBusinessId(id, LocalDateTime.now());
        response.setHasActiveSubscription(activeSubscription.isPresent());
        
        if (activeSubscription.isPresent()) {
            if (activeSubscription.get().getPlan() != null) {
                response.setCurrentSubscriptionPlan(activeSubscription.get().getPlan().getName());
            }
            response.setDaysRemaining(activeSubscription.get().getDaysRemaining());
        } else {
            response.setCurrentSubscriptionPlan("No Active Plan");
            response.setDaysRemaining(0L);
        }

        return response;
    }

    @Override
    public BusinessResponse updateBusiness(UUID id, BusinessUpdateRequest request) {
        // ✅ FIXED: Load with relationships
        Business business = businessRepository.findByIdWithAllRelationships(id)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        businessMapper.updateEntity(request, business);
        Business updatedBusiness = businessRepository.save(business);

        log.info("Business updated successfully: {}", updatedBusiness.getName());
        return businessMapper.toResponse(updatedBusiness);
    }

    @Override
    public BusinessResponse deleteBusiness(UUID id) {
        // ✅ FIXED: Load with relationships
        Business business = businessRepository.findByIdWithAllRelationships(id)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        business.softDelete();
        business = businessRepository.save(business);

        BusinessResponse response = businessMapper.toResponse(business);
        response.setTotalStaff((int) userRepository.countByBusinessIdAndIsDeletedFalse(id));

        // Check subscription status
        var activeSubscription = subscriptionRepository.findCurrentActiveByBusinessId(id, LocalDateTime.now());
        response.setHasActiveSubscription(activeSubscription.isPresent());
        if (activeSubscription.isPresent()) {
            if (activeSubscription.get().getPlan() != null) {
                response.setCurrentSubscriptionPlan(activeSubscription.get().getPlan().getName());
            }
            response.setDaysRemaining(activeSubscription.get().getDaysRemaining());
        }
        
        log.info("Business deleted successfully: {}", business.getName());
        return response;
    }

    private void validateBusinessCreation(BusinessCreateRequest request) {
        List<String> errors = new ArrayList<>();

        // Check required fields
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            errors.add("Business name is required");
        }

        // Check business name uniqueness (case-insensitive)
        if (request.getName() != null && isBusinessNameTaken(request.getName())) {
            errors.add("Business name '" + request.getName() + "' is already taken");
        }

        // Check email format and uniqueness
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (!isValidEmail(request.getEmail())) {
                errors.add("Business email format is invalid");
            } else if (businessRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
                errors.add("Business email '" + request.getEmail() + "' is already registered");
            }
        }

        // Check phone format
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            if (!isValidPhone(request.getPhone())) {
                errors.add("Business phone number format is invalid");
            }
        }

        // Throw validation exception with all errors
        if (!errors.isEmpty()) {
            String errorMessage = "Business validation failed: " + String.join(", ", errors);
            throw new ValidationException(errorMessage);
        }
    }

    private boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return true; // Phone is optional
        }

        // Cambodia phone number patterns: +855, 0, or direct numbers
        String cleanPhone = phone.replaceAll("[^0-9+]", "");

        // Valid patterns for Cambodia
        return cleanPhone.matches("^(\\+855|0)?[1-9][0-9]{7,8}$");
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }

    @Transactional(readOnly = true)
    private boolean isBusinessNameTaken(String name) {
        return businessRepository.existsByNameIgnoreCaseAndIsDeletedFalse(name);
    }

    // ✅ IMPROVED: Better subdomain generation from business name
    private String generateSubdomainFromBusinessName(String businessName) {
        if (businessName == null || businessName.trim().isEmpty()) {
            return "business";
        }

        return businessName.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "") // Remove special characters
                .replaceAll("\\s+", "-")          // Replace spaces with hyphens
                .replaceAll("-{2,}", "-")         // Replace multiple hyphens with single
                .replaceAll("^-+|-+$", "")        // Remove leading/trailing hyphens
                .substring(0, Math.min(businessName.length(), 30)); // Limit length
    }
}