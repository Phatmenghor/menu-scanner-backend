package com.emenu.features.auth.specification;

import com.emenu.features.auth.dto.filter.SubscriptionFilterRequest;
import com.emenu.features.auth.models.Subscription;
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

            // Multiple business IDs filter
            if (filter.getBusinessIds() != null && !filter.getBusinessIds().isEmpty()) {
                predicates.add(root.get("businessId").in(filter.getBusinessIds()));
            }

            // Plan ID filter
            if (filter.getPlanId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("planId"), filter.getPlanId()));
            }

            // Multiple plan IDs filter
            if (filter.getPlanIds() != null && !filter.getPlanIds().isEmpty()) {
                predicates.add(root.get("planId").in(filter.getPlanIds()));
            }

            // ✅ SIMPLIFIED: Active status filter
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

            // Date range filters
            if (filter.getStartDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), filter.getStartDateFrom()));
            }

            if (filter.getStartDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("startDate"), filter.getStartDateTo()));
            }

            if (filter.getEndDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("endDate"), filter.getEndDateFrom()));
            }

            if (filter.getEndDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), filter.getEndDateTo()));
            }

            // Expiring soon filter
            if (filter.getExpiringSoon() != null && filter.getExpiringSoon()) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime futureDate = now.plusDays(filter.getExpiringSoonDays());
                predicates.add(criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("isActive"), true),
                    criteriaBuilder.between(root.get("endDate"), now, futureDate)
                ));
            }

            // ✅ SIMPLIFIED: Basic search across business and plan names
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

    // ✅ SIMPLIFIED: Common specifications
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

    public static Specification<Subscription> isExpired() {
        return (root, query, criteriaBuilder) -> {
            LocalDateTime now = LocalDateTime.now();
            return criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("isDeleted"), false),
                    criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), now)
            );
        };
    }

    public static Specification<Subscription> expiringSoon(int days) {
        return (root, query, criteriaBuilder) -> {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime futureDate = now.plusDays(days);
            return criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("isDeleted"), false),
                    criteriaBuilder.equal(root.get("isActive"), true),
                    criteriaBuilder.between(root.get("endDate"), now, futureDate)
            );
        };
    }

    public static Specification<Subscription> byBusiness(java.util.UUID businessId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("isDeleted"), false),
                        criteriaBuilder.equal(root.get("businessId"), businessId)
                );
    }

    public static Specification<Subscription> byPlan(java.util.UUID planId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("isDeleted"), false),
                        criteriaBuilder.equal(root.get("planId"), planId)
                );
    }
}
