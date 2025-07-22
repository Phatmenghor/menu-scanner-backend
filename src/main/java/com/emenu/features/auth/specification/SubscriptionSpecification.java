package com.emenu.features.auth.specification;

import com.emenu.enums.sub_scription.SubscriptionStatus;
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

            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            if (filter.getBusinessId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("businessId"), filter.getBusinessId()));
            }

            if (filter.getBusinessIds() != null && !filter.getBusinessIds().isEmpty()) {
                predicates.add(root.get("businessId").in(filter.getBusinessIds()));
            }

            if (filter.getPlanId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("planId"), filter.getPlanId()));
            }

            if (filter.getPlanIds() != null && !filter.getPlanIds().isEmpty()) {
                predicates.add(root.get("planId").in(filter.getPlanIds()));
            }

            if (filter.getStatus() != null) {
                predicates.add(createStatusPredicate(filter.getStatus(), root, criteriaBuilder));
            }

            if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
                List<Predicate> statusPredicates = new ArrayList<>();
                for (SubscriptionStatus status : filter.getStatuses()) {
                    statusPredicates.add(createStatusPredicate(status, root, criteriaBuilder));
                }
                predicates.add(criteriaBuilder.or(statusPredicates.toArray(new Predicate[0])));
            }

            if (filter.getAutoRenew() != null) {
                predicates.add(criteriaBuilder.equal(root.get("autoRenew"), filter.getAutoRenew()));
            }

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

            if (filter.getExpiringSoon() != null && filter.getExpiringSoon()) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime futureDate = now.plusDays(filter.getExpiringSoonDays());
                predicates.add(criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("isActive"), true),
                    criteriaBuilder.between(root.get("endDate"), now, futureDate)
                ));
            }

            if (filter.getHasCustomLimits() != null && filter.getHasCustomLimits()) {
                predicates.add(criteriaBuilder.or(
                    criteriaBuilder.isNotNull(root.get("customMaxStaff")),
                    criteriaBuilder.isNotNull(root.get("customMaxMenuItems")),
                    criteriaBuilder.isNotNull(root.get("customMaxTables")),
                    criteriaBuilder.isNotNull(root.get("customDurationDays"))
                ));
            }

            if (StringUtils.hasText(filter.getSearch())) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                
                Join<Object, Object> businessJoin = root.join("business", JoinType.LEFT);
                Join<Object, Object> planJoin = root.join("plan", JoinType.LEFT);
                
                Predicate businessNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(businessJoin.get("name")), searchPattern);
                Predicate planNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(planJoin.get("name")), searchPattern);
                Predicate planDisplayNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(planJoin.get("displayName")), searchPattern);

                predicates.add(criteriaBuilder.or(
                        businessNamePredicate, planNamePredicate, planDisplayNamePredicate
                ));
                
                query.distinct(true);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Predicate createStatusPredicate(SubscriptionStatus status, 
                                                  jakarta.persistence.criteria.Root<Subscription> root, 
                                                  jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder) {
        LocalDateTime now = LocalDateTime.now();
        
        return switch (status) {
            case ACTIVE -> criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("isActive"), true),
                    criteriaBuilder.greaterThan(root.get("endDate"), now)
            );
            case EXPIRED -> criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("isActive"), true),
                    criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), now)
            );
            case CANCELLED -> criteriaBuilder.equal(root.get("isActive"), false);
            case PENDING -> criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("isActive"), false),
                    criteriaBuilder.greaterThan(root.get("startDate"), now)
            );
            case SUSPENDED -> criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("isActive"), false),
                    criteriaBuilder.lessThanOrEqualTo(root.get("startDate"), now),
                    criteriaBuilder.greaterThan(root.get("endDate"), now)
            );
            case TRIAL -> criteriaBuilder.equal(root.get("isTrial"), true);
        };
    }

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
}