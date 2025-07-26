package com.emenu.features.subdomain.specification;

import com.emenu.enums.subdomain.SubdomainStatus;
import com.emenu.features.subdomain.dto.filter.SubdomainFilterRequest;
import com.emenu.features.subdomain.models.Subdomain;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SubdomainSpecification {

    public static Specification<Subdomain> buildSpecification(SubdomainFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base condition: not deleted
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            // Business ID filter
            if (filter.getBusinessId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("businessId"), filter.getBusinessId()));
            }

            // Multiple business IDs filter
            if (filter.getBusinessIds() != null && !filter.getBusinessIds().isEmpty()) {
                predicates.add(root.get("businessId").in(filter.getBusinessIds()));
            }

            // Status filter
            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            // Multiple statuses filter
            if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
                predicates.add(root.get("status").in(filter.getStatuses()));
            }

            // Active status filter
            if (filter.getIsActive() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), filter.getIsActive()));
            }

            // Domain verified filter
            if (filter.getDomainVerified() != null) {
                predicates.add(criteriaBuilder.equal(root.get("domainVerified"), filter.getDomainVerified()));
            }

            // SSL enabled filter
            if (filter.getSslEnabled() != null) {
                predicates.add(criteriaBuilder.equal(root.get("sslEnabled"), filter.getSslEnabled()));
            }

            // Has active subscription filter
            if (filter.getHasActiveSubscription() != null) {
                Join<Object, Object> businessJoin = root.join("business", JoinType.LEFT);
                
                if (filter.getHasActiveSubscription()) {
                    // Has active subscription
                    predicates.add(criteriaBuilder.and(
                        criteriaBuilder.equal(businessJoin.get("isSubscriptionActive"), true),
                        criteriaBuilder.greaterThan(businessJoin.get("subscriptionEndDate"), LocalDateTime.now())
                    ));
                } else {
                    // No active subscription
                    predicates.add(criteriaBuilder.or(
                        criteriaBuilder.equal(businessJoin.get("isSubscriptionActive"), false),
                        criteriaBuilder.isNull(businessJoin.get("subscriptionEndDate")),
                        criteriaBuilder.lessThanOrEqualTo(businessJoin.get("subscriptionEndDate"), LocalDateTime.now())
                    ));
                }
            }

            // Global search filter (searches across subdomain, business name, custom domain)
            if (StringUtils.hasText(filter.getSearch())) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                
                Join<Object, Object> businessJoin = root.join("business", JoinType.LEFT);
                
                Predicate subdomainPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("subdomain")), searchPattern);
                Predicate businessNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(businessJoin.get("name")), searchPattern);
                Predicate customDomainPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("customDomain")), searchPattern);

                predicates.add(criteriaBuilder.or(
                        subdomainPredicate, businessNamePredicate, customDomainPredicate
                ));
                
                query.distinct(true);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    // Common specifications for quick queries
    public static Specification<Subdomain> isActive() {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("isActive"), true),
                criteriaBuilder.equal(root.get("status"), SubdomainStatus.ACTIVE)
            );
    }

    public static Specification<Subdomain> isAccessible() {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("isActive"), true),
                criteriaBuilder.equal(root.get("status"), SubdomainStatus.ACTIVE)
            );
    }

    public static Specification<Subdomain> withActiveSubscription() {
        return (root, query, criteriaBuilder) -> {
            Join<Object, Object> businessJoin = root.join("business", JoinType.INNER);
            LocalDateTime now = LocalDateTime.now();
            
            return criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(businessJoin.get("isDeleted"), false),
                criteriaBuilder.equal(businessJoin.get("isSubscriptionActive"), true),
                criteriaBuilder.greaterThan(businessJoin.get("subscriptionEndDate"), now)
            );
        };
    }

    public static Specification<Subdomain> byStatus(SubdomainStatus status) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("status"), status)
            );
    }

    public static Specification<Subdomain> byBusiness(java.util.UUID businessId) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("businessId"), businessId)
            );
    }

    public static Specification<Subdomain> domainVerified() {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("domainVerified"), true)
            );
    }

    public static Specification<Subdomain> sslEnabled() {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("sslEnabled"), true)
            );
    }

    public static Specification<Subdomain> recentlyAccessed(LocalDateTime since) {
        return (root, query, criteriaBuilder) -> {
            query.orderBy(criteriaBuilder.desc(root.get("lastAccessed")));
            return criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.greaterThan(root.get("lastAccessed"), since)
            );
        };
    }

    public static Specification<Subdomain> bySubdomainPattern(String pattern) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("subdomain")), 
                                   "%" + pattern.toLowerCase() + "%")
            );
    }
}