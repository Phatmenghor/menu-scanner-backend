package com.emenu.features.auth.specification;

import com.emenu.enums.sub_scription.SubscriptionPlanStatus;
import com.emenu.features.auth.dto.filter.SubscriptionPlanFilterRequest;
import com.emenu.features.auth.models.SubscriptionPlan;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionPlanSpecification {

    public static Specification<SubscriptionPlan> buildSpecification(SubscriptionPlanFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            if (filter.getStatus() != null) {
                predicates.add(createStatusPredicate(filter.getStatus(), root, criteriaBuilder));
            }

            if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
                List<Predicate> statusPredicates = new ArrayList<>();
                for (SubscriptionPlanStatus status : filter.getStatuses()) {
                    statusPredicates.add(createStatusPredicate(status, root, criteriaBuilder));
                }
                predicates.add(criteriaBuilder.or(statusPredicates.toArray(new Predicate[0])));
            }

            if (filter.getPublicOnly() != null && filter.getPublicOnly()) {
                predicates.add(criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("isActive"), true),
                    criteriaBuilder.equal(root.get("isCustom"), false)
                ));
            }

            if (filter.getFreeOnly() != null && filter.getFreeOnly()) {
                predicates.add(criteriaBuilder.equal(root.get("price"), BigDecimal.ZERO));
            }

            if (filter.getBusinessId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isCustom"), true));
            }

            if (filter.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));
            }

            if (filter.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));
            }

            if (filter.getMinDurationDays() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("durationDays"), filter.getMinDurationDays()));
            }

            if (filter.getMaxDurationDays() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("durationDays"), filter.getMaxDurationDays()));
            }

            if (StringUtils.hasText(filter.getSearch())) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), searchPattern);
                Predicate displayNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("displayName")), searchPattern);
                Predicate descriptionPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), searchPattern);

                predicates.add(criteriaBuilder.or(
                        namePredicate, displayNamePredicate, descriptionPredicate
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Predicate createStatusPredicate(SubscriptionPlanStatus status, 
                                                  jakarta.persistence.criteria.Root<SubscriptionPlan> root, 
                                                  jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder) {
        return switch (status) {
            case ACTIVE -> criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("isActive"), true),
                    criteriaBuilder.equal(root.get("isCustom"), false)
            );
            case INACTIVE -> criteriaBuilder.equal(root.get("isActive"), false);
            case CUSTOM -> criteriaBuilder.equal(root.get("isCustom"), true);
            case TRIAL -> criteriaBuilder.equal(root.get("isTrial"), true);
            case DEFAULT -> criteriaBuilder.equal(root.get("isDefault"), true);
            case ARCHIVED -> criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("isActive"), false),
                    criteriaBuilder.equal(root.get("isDeleted"), false)
            );
        };
    }

    public static Specification<SubscriptionPlan> isActive() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("isActive"), true)
            );
    }

    public static Specification<SubscriptionPlan> isPublic() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("isActive"), true),
                criteriaBuilder.equal(root.get("isCustom"), false)
            );
    }

    public static Specification<SubscriptionPlan> isCustom() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("isCustom"), true)
            );
    }

    public static Specification<SubscriptionPlan> isFree() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("price"), BigDecimal.ZERO)
            );
    }

    public static Specification<SubscriptionPlan> isTrial() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("isTrial"), true)
            );
    }

    public static Specification<SubscriptionPlan> isDefault() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("isDefault"), true)
            );
    }
}
