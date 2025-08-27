package com.emenu.features.subscription.specification;

import com.emenu.features.subscription.dto.filter.SubscriptionFilterRequest;
import com.emenu.features.subscription.models.Subscription;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionSpecification {

    public static Specification<Subscription> buildSpecification(SubscriptionFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base condition: not deleted
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            // Business ID filter
            if (filter.getBusinessId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("businessId"), filter.getBusinessId()));
            }

            // Plan ID filter
            if (filter.getPlanId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("planId"), filter.getPlanId()));
            }

            // Active status filter
            if (filter.getIsActive() != null) {
                if (filter.getIsActive()) {
                    // Active and not expired
                    predicates.add(criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("isActive"), true),
                        criteriaBuilder.greaterThan(root.get("endDate"), LocalDateTime.now())
                    ));
                } else {
                    // Inactive or expired
                    predicates.add(criteriaBuilder.or(
                        criteriaBuilder.equal(root.get("isActive"), false),
                        criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), LocalDateTime.now())
                    ));
                }
            }

            // Auto-renew filter
            if (filter.getAutoRenew() != null) {
                predicates.add(criteriaBuilder.equal(root.get("autoRenew"), filter.getAutoRenew()));
            }

            // Simple date range filtering - only 2 fields
            if (filter.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), filter.getStartDate()));
            }

            if (filter.getToDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("startDate"), filter.getToDate()));
            }

            // Basic search across business and plan names
            if (StringUtils.hasText(filter.getSearch())) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                
                Join<Object, Object> businessJoin = root.join("business", JoinType.LEFT);
                Join<Object, Object> planJoin = root.join("plan", JoinType.LEFT);
                
                Predicate businessNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(businessJoin.get("name")), searchPattern);
                Predicate planNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(planJoin.get("name")), searchPattern);

                predicates.add(criteriaBuilder.or(businessNamePredicate, planNamePredicate));
                
                query.distinct(true);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    // Common specifications for basic queries
    public static Specification<Subscription> isActive() {
        return (root, query, criteriaBuilder) -> {
            LocalDateTime now = LocalDateTime.now();
            return criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("isDeleted"), false),
                    criteriaBuilder.equal(root.get("isActive"), true),
                    criteriaBuilder.greaterThan(root.get("endDate"), now)
            );
        };
    }

}
