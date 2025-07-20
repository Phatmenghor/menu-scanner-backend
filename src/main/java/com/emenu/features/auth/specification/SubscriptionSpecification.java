package com.emenu.features.auth.specification;

import com.emenu.features.auth.dto.filter.SubscriptionFilterRequest;
import com.emenu.features.auth.models.Subscription;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

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
                predicates.add(criteriaBuilder.equal(root.get("isActive"), filter.getIsActive()));
            }

            // Auto renew filter
            if (filter.getAutoRenew() != null) {
                predicates.add(criteriaBuilder.equal(root.get("autoRenew"), filter.getAutoRenew()));
            }

            // Trial filter
            if (filter.getIsTrial() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isTrial"), filter.getIsTrial()));
            }

            // Date filters
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

            // Expired filter
            if (filter.getIsExpired() != null) {
                LocalDateTime now = LocalDateTime.now();
                if (filter.getIsExpired()) {
                    predicates.add(criteriaBuilder.lessThan(root.get("endDate"), now));
                } else {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("endDate"), now));
                }
            }

            // Expiring soon filter (within next 7 days)
            if (filter.getExpiringSoon() != null && filter.getExpiringSoon()) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime weekFromNow = now.plusDays(7);
                predicates.add(criteriaBuilder.between(root.get("endDate"), now, weekFromNow));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    // Specific specifications for common queries
    public static Specification<Subscription> isActive() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("isActive"), true)
            );
    }

    public static Specification<Subscription> isExpired() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.lessThan(root.get("endDate"), LocalDateTime.now())
            );
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

    public static Specification<Subscription> byPlan(java.util.UUID planId) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("planId"), planId)
            );
    }
}