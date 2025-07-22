package com.emenu.features.auth.specification;

import com.emenu.features.auth.dto.filter.BusinessFilterRequest;
import com.emenu.features.auth.models.Business;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BusinessSpecification {

    public static Specification<Business> buildSpecification(BusinessFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base condition: not deleted
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            // Status filter
            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            // Active subscription filter
            if (filter.getHasActiveSubscription() != null) {
                if (filter.getHasActiveSubscription()) {
                    // Has active subscription
                    predicates.add(criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("isSubscriptionActive"), true),
                        criteriaBuilder.greaterThan(root.get("subscriptionEndDate"), LocalDateTime.now())
                    ));
                } else {
                    // No active subscription
                    predicates.add(criteriaBuilder.or(
                        criteriaBuilder.equal(root.get("isSubscriptionActive"), false),
                        criteriaBuilder.isNull(root.get("subscriptionEndDate")),
                        criteriaBuilder.lessThanOrEqualTo(root.get("subscriptionEndDate"), LocalDateTime.now())
                    ));
                }
            }

            // Global search filter (searches across name, email, description)
            if (StringUtils.hasText(filter.getSearch())) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), searchPattern);
                Predicate emailPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")), searchPattern);
                Predicate descriptionPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), searchPattern);

                predicates.add(criteriaBuilder.or(
                        namePredicate, emailPredicate, descriptionPredicate
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}