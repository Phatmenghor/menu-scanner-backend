package com.emenu.features.order.specification;

import com.emenu.enums.common.Status;
import com.emenu.features.order.dto.filter.DeliveryOptionFilterRequest;
import com.emenu.features.order.dto.filter.base.DeliveryOptionFilterBase;
import com.emenu.features.order.models.DeliveryOption;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class DeliveryOptionSpecification {

    public static Specification<DeliveryOption> buildSpecification(DeliveryOptionFilterBase filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base condition: not deleted
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            // Business ID filter
            if (filter.getBusinessId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("businessId"), filter.getBusinessId()));
            }

            // Multiple status filter
            if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
                predicates.add(root.get("status").in(filter.getStatuses()));
            }

            // Price range filter
            if (filter.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("price"), filter.getMinPrice()));
            }

            if (filter.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("price"), filter.getMaxPrice()));
            }

            // Global search filter
            if (StringUtils.hasText(filter.getSearch())) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                
                Join<Object, Object> businessJoin = root.join("business", JoinType.LEFT);
                
                Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), searchPattern);
                Predicate descriptionPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), searchPattern);
                Predicate businessNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(businessJoin.get("name")), searchPattern);

                predicates.add(criteriaBuilder.or(
                        namePredicate, descriptionPredicate, businessNamePredicate
                ));
                
                query.distinct(true);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
