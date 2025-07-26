package com.emenu.features.subdomain.service.impl;

import com.emenu.exception.custom.NotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.subdomain.dto.filter.SubdomainFilterRequest;
import com.emenu.features.subdomain.dto.request.SubdomainCreateRequest;
import com.emenu.features.subdomain.dto.response.SubdomainCheckResponse;
import com.emenu.features.subdomain.dto.response.SubdomainResponse;
import com.emenu.features.subdomain.dto.update.SubdomainUpdateRequest;
import com.emenu.features.subdomain.mapper.SubdomainMapper;
import com.emenu.features.subdomain.models.Subdomain;
import com.emenu.features.subdomain.repository.SubdomainRepository;
import com.emenu.features.subdomain.service.SubdomainService;
import com.emenu.features.subdomain.specification.SubdomainSpecification;
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
    private final SecurityUtils securityUtils;

    @Override
    public SubdomainResponse createSubdomain(SubdomainCreateRequest request) {
        log.info("Creating subdomain: {} for business: {}", request.getSubdomain(), request.getBusinessId());

        // Validate business exists
        Business business = businessRepository.findByIdAndIsDeletedFalse(request.getBusinessId())
                .orElseThrow(() -> new NotFoundException("Business not found"));

        // Check if business already has a subdomain
        if (subdomainRepository.existsByBusinessIdAndIsDeletedFalse(request.getBusinessId())) {
            throw new ValidationException("Business already has a subdomain");
        }

        // Check subdomain availability
        if (!isSubdomainAvailable(request.getSubdomain())) {
            throw new ValidationException("Subdomain '" + request.getSubdomain() + "' is already taken");
        }

        // Create subdomain
        Subdomain subdomain = subdomainMapper.toEntity(request);
        subdomain.setSubdomain(request.getSubdomain().toLowerCase().trim());

        Subdomain savedSubdomain = subdomainRepository.save(subdomain);
        log.info("Subdomain created successfully: {} for business: {}", 
                savedSubdomain.getSubdomain(), business.getName());

        return subdomainMapper.toResponse(savedSubdomain);
    }

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
        Subdomain subdomain = findSubdomainById(id);
        return subdomainMapper.toResponse(subdomain);
    }

    @Override
    @Transactional(readOnly = true)
    public SubdomainResponse getSubdomainByName(String subdomainName) {
        Subdomain subdomain = subdomainRepository.findBySubdomainAndIsDeletedFalse(subdomainName)
                .orElseThrow(() -> new NotFoundException("Subdomain not found: " + subdomainName));
        return subdomainMapper.toResponse(subdomain);
    }

    @Override
    public SubdomainResponse updateSubdomain(UUID id, SubdomainUpdateRequest request) {
        Subdomain subdomain = findSubdomainById(id);

        // Check subdomain name availability if being changed
        if (request.getSubdomain() != null && 
            !request.getSubdomain().equals(subdomain.getSubdomain())) {
            if (!isSubdomainAvailable(request.getSubdomain())) {
                throw new ValidationException("Subdomain '" + request.getSubdomain() + "' is already taken");
            }
        }

        subdomainMapper.updateEntity(request, subdomain);
        Subdomain updatedSubdomain = subdomainRepository.save(subdomain);

        log.info("Subdomain updated successfully: {}", updatedSubdomain.getSubdomain());
        return subdomainMapper.toResponse(updatedSubdomain);
    }

    @Override
    public SubdomainResponse deleteSubdomain(UUID id) {
        Subdomain subdomain = findSubdomainById(id);
        
        SubdomainResponse response = subdomainMapper.toResponse(subdomain);
        
        subdomain.softDelete();
        subdomainRepository.save(subdomain);

        log.info("Subdomain deleted successfully: {}", subdomain.getSubdomain());
        return response;
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
    public PaginationResponse<SubdomainResponse> getCurrentUserBusinessSubdomains(SubdomainFilterRequest filter) {
        User currentUser = securityUtils.getCurrentUser();
        
        if (currentUser.getBusinessId() == null) {
            throw new ValidationException("User is not associated with any business");
        }

        filter.setBusinessId(currentUser.getBusinessId());
        return getAllSubdomains(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public SubdomainCheckResponse checkSubdomainAccess(String subdomainName) {
        log.debug("Checking subdomain access for: {}", subdomainName);

        // Find subdomain with business info
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
    public SubdomainResponse verifyDomain(UUID id) {
        Subdomain subdomain = findSubdomainById(id);
        
        subdomain.verify();
        Subdomain verifiedSubdomain = subdomainRepository.save(subdomain);
        
        log.info("Domain verified successfully: {}", verifiedSubdomain.getSubdomain());
        return subdomainMapper.toResponse(verifiedSubdomain);
    }

    @Override
    public SubdomainResponse enableSSL(UUID id) {
        Subdomain subdomain = findSubdomainById(id);
        
        subdomain.setSslEnabled(true);
        Subdomain updatedSubdomain = subdomainRepository.save(subdomain);
        
        log.info("SSL enabled for subdomain: {}", updatedSubdomain.getSubdomain());
        return subdomainMapper.toResponse(updatedSubdomain);
    }

    @Override
    public SubdomainResponse suspendSubdomain(UUID id, String reason) {
        Subdomain subdomain = findSubdomainById(id);
        
        subdomain.suspend(reason);
        Subdomain suspendedSubdomain = subdomainRepository.save(subdomain);
        
        log.info("Subdomain suspended: {} - Reason: {}", suspendedSubdomain.getSubdomain(), reason);
        return subdomainMapper.toResponse(suspendedSubdomain);
    }

    @Override
    public SubdomainResponse activateSubdomain(UUID id) {
        Subdomain subdomain = findSubdomainById(id);
        
        subdomain.activate();
        Subdomain activatedSubdomain = subdomainRepository.save(subdomain);
        
        log.info("Subdomain activated: {}", activatedSubdomain.getSubdomain());
        return subdomainMapper.toResponse(activatedSubdomain);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalSubdomainsCount() {
        return subdomainRepository.countTotalSubdomains();
    }

    @Override
    @Transactional(readOnly = true)
    public long getActiveSubdomainsCount() {
        return subdomainRepository.countActiveSubdomains();
    }

    @Override
    public SubdomainResponse createSubdomainForBusiness(UUID businessId, String preferredSubdomain) {
        log.info("Auto-creating subdomain for business: {} with preferred name: {}", businessId, preferredSubdomain);

        // Check if business already has a subdomain
        if (subdomainRepository.existsByBusinessIdAndIsDeletedFalse(businessId)) {
            log.debug("Business already has a subdomain, returning existing one");
            return getSubdomainByBusinessId(businessId);
        }

        // Generate available subdomain name (with formatting)
        String availableSubdomain = generateAvailableSubdomain(preferredSubdomain);

        SubdomainCreateRequest request = new SubdomainCreateRequest();
        request.setBusinessId(businessId);
        request.setSubdomain(availableSubdomain);
        request.setNotes("Auto-created during business registration");

        return createSubdomain(request);
    }

    @Override
    public SubdomainResponse createExactSubdomainForBusiness(UUID businessId, String exactSubdomain) {
        log.info("Creating exact subdomain for business: {} with exact name: {}", businessId, exactSubdomain);

        // Validate business exists
        Business business = businessRepository.findByIdAndIsDeletedFalse(businessId)
                .orElseThrow(() -> new NotFoundException("Business not found"));

        // Check if business already has a subdomain
        if (subdomainRepository.existsByBusinessIdAndIsDeletedFalse(businessId)) {
            log.debug("Business already has a subdomain, returning existing one");
            return getSubdomainByBusinessId(businessId);
        }

        // ✅ MINIMAL CLEANING: Only lowercase and remove truly invalid characters (no hyphens added)
        String cleanedSubdomain = exactSubdomain.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9]", ""); // Remove only truly invalid characters, keep alphanumeric

        // Ensure minimum length
        if (cleanedSubdomain.length() < 3) {
            cleanedSubdomain = cleanedSubdomain + "123"; // Add numbers to meet minimum length
        }

        // If exact subdomain is taken, add a number suffix
        String finalSubdomain = cleanedSubdomain;
        int counter = 1;
        while (!isSubdomainAvailable(finalSubdomain)) {
            finalSubdomain = cleanedSubdomain + counter;
            counter++;
            
            // Prevent infinite loop
            if (counter > 1000) {
                finalSubdomain = cleanedSubdomain + System.currentTimeMillis() % 10000;
                break;
            }
        }

        SubdomainCreateRequest request = new SubdomainCreateRequest();
        request.setBusinessId(businessId);
        request.setSubdomain(finalSubdomain);
        request.setNotes("Created by platform admin with exact input: " + exactSubdomain);

        SubdomainResponse response = createSubdomain(request);
        log.info("Exact subdomain created successfully: {} (from input: {}) for business: {}", 
                finalSubdomain, exactSubdomain, business.getName());

        return response;
    }

    // Private helper methods
    private Subdomain findSubdomainById(UUID id) {
        return subdomainRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Subdomain not found"));
    }

    private boolean isValidSubdomainFormat(String subdomain) {
        if (subdomain == null || subdomain.length() < 3 || subdomain.length() > 63) {
            return false;
        }
        
        // Must start and end with alphanumeric, can contain hyphens in between
        return subdomain.matches("^[a-z0-9][a-z0-9-]*[a-z0-9]$") || subdomain.matches("^[a-z0-9]$");
    }

    private String generateAvailableSubdomain(String preferredSubdomain) {
        if (preferredSubdomain == null || preferredSubdomain.trim().isEmpty()) {
            preferredSubdomain = "business";
        }

        String baseSubdomain = cleanSubdomainName(preferredSubdomain);
        String candidateSubdomain = baseSubdomain;
        int counter = 1;

        while (!isSubdomainAvailable(candidateSubdomain)) {
            candidateSubdomain = baseSubdomain + "-" + counter;
            counter++;
            
            // Prevent infinite loop
            if (counter > 1000) {
                candidateSubdomain = "business-" + System.currentTimeMillis();
                break;
            }
        }

        return candidateSubdomain;
    }

    private String cleanSubdomainName(String subdomain) {
        if (subdomain == null) return "business";
        
        return subdomain.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9-]", "") // Remove invalid characters
                .replaceAll("^-+|-+$", "")     // Remove leading/trailing hyphens
                .replaceAll("-{2,}", "-");     // Replace multiple hyphens with single
    }
}