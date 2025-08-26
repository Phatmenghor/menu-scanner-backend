package com.emenu.features.subscription.specification;

import com.emenu.enums.sub_scription.SubscriptionPlanStatus;
import com.emenu.features.subscription.dto.filter.SubscriptionPlanFilterRequest;
import com.emenu.features.subscription.models.SubscriptionPlan;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionPlanSpecification {

    public static Specification<SubscriptionPlan> buildSpecification(SubscriptionPlanFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
                predicates.add(root.get("status").in(filter.getStatuses()));
            }

            if (filter.getFreeOnly() != null && filter.getFreeOnly()) {
                predicates.add(criteriaBuilder.equal(root.get("price"), java.math.BigDecimal.ZERO));
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
                Predicate descriptionPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), searchPattern);

                predicates.add(criteriaBuilder.or(namePredicate, descriptionPredicate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<SubscriptionPlan> isPublic() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("status"), SubscriptionPlanStatus.PUBLIC)
            );
    }

    public static Specification<SubscriptionPlan> isPrivate() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("status"), SubscriptionPlanStatus.PRIVATE)
            );
    }

    public static Specification<SubscriptionPlan> isFree() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("price"), java.math.BigDecimal.ZERO)
            );
    }
}