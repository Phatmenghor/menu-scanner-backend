package com.emenu.features.subdomain.mapper;

import com.emenu.features.subdomain.dto.request.SubdomainCreateRequest;
import com.emenu.features.subdomain.dto.response.SubdomainResponse;
import com.emenu.features.subdomain.dto.update.SubdomainUpdateRequest;
import com.emenu.features.subdomain.models.Subdomain;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class SubdomainMapper {

    @Autowired
    protected PaginationMapper paginationMapper;

    // Create mapping
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "domainVerified", constant = "false")
    @Mapping(target = "sslEnabled", constant = "false")
    @Mapping(target = "verificationToken", ignore = true)
    @Mapping(target = "verifiedAt", ignore = true)
    @Mapping(target = "lastAccessed", ignore = true)
    @Mapping(target = "accessCount", constant = "0L")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    public abstract Subdomain toEntity(SubdomainCreateRequest request);

    // Response mapping
    @Mapping(source = "business.name", target = "businessName")
    public abstract SubdomainResponse toResponse(Subdomain subdomain);

    public abstract List<SubdomainResponse> toResponseList(List<Subdomain> subdomains);

    // Update mapping
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "domainVerified", ignore = true)
    @Mapping(target = "verificationToken", ignore = true)
    @Mapping(target = "verifiedAt", ignore = true)
    @Mapping(target = "lastAccessed", ignore = true)
    @Mapping(target = "accessCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    public abstract void updateEntity(SubdomainUpdateRequest request, @MappingTarget Subdomain subdomain);

    // After mapping to set computed fields
    @AfterMapping
    protected void setComputedFields(@MappingTarget SubdomainResponse response, Subdomain subdomain) {
        // Set computed fields
        response.setFullDomain(subdomain.getFullDomain());
        response.setFullUrl(subdomain.getFullUrl());
        response.setIsAccessible(subdomain.isAccessible());
        response.setCanAccess(subdomain.canAccess());
        
        // Set subscription-related fields
        if (subdomain.getBusiness() != null) {
            response.setBusinessHasActiveSubscription(subdomain.getBusiness().hasActiveSubscription());
            response.setHasActiveSubscription(subdomain.getBusiness().hasActiveSubscription());
            
            // Get current subscription plan if available
            if (subdomain.getBusiness().hasActiveSubscription() && 
                subdomain.getBusiness().getSubscriptions() != null) {
                subdomain.getBusiness().getSubscriptions().stream()
                        .filter(sub -> sub.getIsActive() && !sub.isExpired())
                        .findFirst()
                        .ifPresent(subscription -> {
                            if (subscription.getPlan() != null) {
                                response.setCurrentSubscriptionPlan(subscription.getPlan().getName());
                            }
                            response.setSubscriptionDaysRemaining(subscription.getDaysRemaining());
                        });
            }
        } else {
            response.setBusinessHasActiveSubscription(false);
            response.setHasActiveSubscription(false);
            response.setSubscriptionDaysRemaining(0L);
        }
    }

    // Pagination response mapping
    public PaginationResponse<SubdomainResponse> toPaginationResponse(Page<Subdomain> subdomainPage) {
        return paginationMapper.toPaginationResponse(subdomainPage, this::toResponseList);
    }

    // Helper method to clean subdomain name
    protected String cleanSubdomainName(String subdomain) {
        if (subdomain == null) return null;
        
        return subdomain.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9-]", "") // Remove invalid characters
                .replaceAll("^-+|-+$", "")     // Remove leading/trailing hyphens
                .replaceAll("-{2,}", "-");     // Replace multiple hyphens with single
    }

    // Custom mapping for subdomain creation with validation
    @Named("createSubdomainEntity")
    public Subdomain createSubdomainEntity(SubdomainCreateRequest request) {
        Subdomain subdomain = toEntity(request);
        
        // Clean and validate subdomain name
        String cleanedSubdomain = cleanSubdomainName(request.getSubdomain());
        subdomain.setSubdomain(cleanedSubdomain);
        
        return subdomain;
    }
}