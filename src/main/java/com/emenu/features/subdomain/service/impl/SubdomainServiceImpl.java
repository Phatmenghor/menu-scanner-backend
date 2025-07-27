package com.emenu.features.subdomain.service.impl;

import com.emenu.exception.custom.NotFoundException;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.subdomain.dto.filter.SubdomainFilterRequest;
import com.emenu.features.subdomain.dto.response.SubdomainCheckResponse;
import com.emenu.features.subdomain.dto.response.SubdomainResponse;
import com.emenu.features.subdomain.mapper.SubdomainMapper;
import com.emenu.features.subdomain.models.Subdomain;
import com.emenu.features.subdomain.repository.SubdomainRepository;
import com.emenu.features.subdomain.service.SubdomainService;
import com.emenu.features.subdomain.specification.SubdomainSpecification;
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
public class SubdomainServiceImpl implements SubdomainService {

    private final SubdomainRepository subdomainRepository;
    private final BusinessRepository businessRepository;
    private final SubdomainMapper subdomainMapper;

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<SubdomainResponse> getAllSubdomains(SubdomainFilterRequest filter) {
        Specification<Subdomain> spec = SubdomainSpecification.buildSpecification(filter);
        
        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(
                pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        Page<Subdomain> subdomainPage = subdomainRepository.findAll(spec, pageable);
        return subdomainMapper.toPaginationResponse(subdomainPage);
    }

    @Override
    @Transactional(readOnly = true)
    public SubdomainResponse getSubdomainById(UUID id) {
        Subdomain subdomain = subdomainRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Subdomain not found"));
        return subdomainMapper.toResponse(subdomain);
    }

    @Override
    @Transactional(readOnly = true)
    public SubdomainResponse getSubdomainByBusinessId(UUID businessId) {
        Subdomain subdomain = subdomainRepository.findByBusinessIdAndIsDeletedFalse(businessId)
                .orElseThrow(() -> new NotFoundException("No subdomain found for business"));
        return subdomainMapper.toResponse(subdomain);
    }

    @Override
    @Transactional(readOnly = true)
    public SubdomainCheckResponse checkSubdomainAccess(String subdomainName) {
        log.debug("Checking subdomain access for: {}", subdomainName);

        var subdomainOpt = subdomainRepository.findBySubdomainWithBusiness(subdomainName);

        if (subdomainOpt.isEmpty()) {
            log.debug("Subdomain not found: {}", subdomainName);
            return SubdomainCheckResponse.notFound(subdomainName);
        }

        Subdomain subdomain = subdomainOpt.get();
        Business business = subdomain.getBusiness();

        // Increment access count asynchronously
        try {
            subdomainRepository.incrementAccessCount(subdomain.getId(), LocalDateTime.now());
        } catch (Exception e) {
            log.warn("Failed to increment access count for subdomain: {}", subdomainName, e);
        }

        // Check if subdomain is suspended
        if (!subdomain.isAccessible()) {
            log.debug("Subdomain is suspended: {}", subdomainName);
            return SubdomainCheckResponse.suspended(subdomainName, business.getName(), business.getId());
        }

        // Check subscription status
        if (!business.hasActiveSubscription()) {
            log.debug("Business subscription expired for subdomain: {}", subdomainName);
            return SubdomainCheckResponse.subscriptionExpired(subdomainName, business.getName(), business.getId());
        }

        // All checks passed - domain is accessible
        log.debug("Subdomain access granted: {}", subdomainName);
        return SubdomainCheckResponse.accessible(subdomainName, business.getName(), business.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSubdomainAvailable(String subdomainName) {
        if (subdomainName == null || subdomainName.trim().isEmpty()) {
            return false;
        }
        
        String cleanedSubdomain = subdomainName.toLowerCase().trim();
        
        // Check format
        if (!isValidSubdomainFormat(cleanedSubdomain)) {
            return false;
        }
        
        // Check if already taken
        return !subdomainRepository.existsBySubdomainAndIsDeletedFalse(cleanedSubdomain);
    }

    @Override
    public SubdomainResponse createSubdomainForBusiness(UUID businessId, String preferredSubdomain) {
        log.info("✅ Auto-creating subdomain for business: {} with preferred name: {}", businessId, preferredSubdomain);

        // Validate business exists
        Business business = businessRepository.findByIdAndIsDeletedFalse(businessId)
                .orElseThrow(() -> new NotFoundException("Business not found"));

        // Check if business already has a subdomain
        if (subdomainRepository.existsByBusinessIdAndIsDeletedFalse(businessId)) {
            log.debug("Business already has a subdomain, returning existing one");
            return getSubdomainByBusinessId(businessId);
        }

        // Generate available subdomain name
        String availableSubdomain = generateAvailableSubdomain(preferredSubdomain);

        // Create subdomain entity
        Subdomain subdomain = new Subdomain();
        subdomain.setSubdomain(availableSubdomain);
        subdomain.setBusinessId(businessId);
        subdomain.setStatus(com.emenu.enums.subdomain.SubdomainStatus.ACTIVE); // Always active
        subdomain.setAccessCount(0L);
        subdomain.setNotes("Auto-created during business registration");

        Subdomain savedSubdomain = subdomainRepository.save(subdomain);
        
        log.info("✅ Subdomain created successfully: {} for business: {}", 
                availableSubdomain, business.getName());

        return subdomainMapper.toResponse(savedSubdomain);
    }

    // Private helper methods
    private boolean isValidSubdomainFormat(String subdomain) {
        if (subdomain == null || subdomain.length() < 3 || subdomain.length() > 63) {
            return false;
        }
        
        // Must start and end with alphanumeric, can contain hyphens in between
        return subdomain.matches("^[a-z0-9][a-z0-9-]*[a-z0-9]$") || subdomain.matches("^[a-z0-9]{3,}$");
    }

    private String generateAvailableSubdomain(String preferredSubdomain) {
        if (preferredSubdomain == null || preferredSubdomain.trim().isEmpty()) {
            preferredSubdomain = "restaurant";
        }

        String baseSubdomain = cleanSubdomainName(preferredSubdomain);
        String candidateSubdomain = baseSubdomain;
        int counter = 1;

        while (!isSubdomainAvailable(candidateSubdomain)) {
            candidateSubdomain = baseSubdomain + counter;
            counter++;
            
            // Prevent infinite loop
            if (counter > 1000) {
                candidateSubdomain = "restaurant" + System.currentTimeMillis();
                break;
            }
        }

        return candidateSubdomain;
    }

    private String cleanSubdomainName(String subdomain) {
        if (subdomain == null) return "restaurant";
        
        return subdomain.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9]", "")      // Remove all non-alphanumeric
                .substring(0, Math.min(subdomain.length(), 20)); // Limit length
    }
}