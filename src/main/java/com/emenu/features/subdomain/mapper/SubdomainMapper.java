package com.emenu.features.subdomain.mapper;

import com.emenu.features.subdomain.dto.response.SubdomainResponse;
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

    // Response mapping
    @Mapping(source = "business.name", target = "businessName")
    public abstract SubdomainResponse toResponse(Subdomain subdomain);

    public abstract List<SubdomainResponse> toResponseList(List<Subdomain> subdomains);

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
            response.setHasActiveSubscription(false);
            response.setSubscriptionDaysRemaining(0L);
        }
    }

    // Pagination response mapping
    public PaginationResponse<SubdomainResponse> toPaginationResponse(Page<Subdomain> subdomainPage) {
        return paginationMapper.toPaginationResponse(subdomainPage, this::toResponseList);
    }
}