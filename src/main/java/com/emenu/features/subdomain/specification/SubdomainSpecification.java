package com.emenu.features.subdomain.specification;

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

            // Status filter
            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
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

            // Global search filter
            if (StringUtils.hasText(filter.getSearch())) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                
                Join<Object, Object> businessJoin = root.join("business", JoinType.LEFT);
                
                Predicate subdomainPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("subdomain")), searchPattern);
                Predicate businessNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(businessJoin.get("name")), searchPattern);

                predicates.add(criteriaBuilder.or(subdomainPredicate, businessNamePredicate));
                
                query.distinct(true);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}