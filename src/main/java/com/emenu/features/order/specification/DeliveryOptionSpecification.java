package com.emenu.features.order.specification;

import com.emenu.enums.common.Status;
import com.emenu.features.order.dto.filter.DeliveryOptionAllFilterRequest;
import com.emenu.features.order.dto.filter.DeliveryOptionFilterRequest;
import com.emenu.features.order.models.DeliveryOption;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA Specification builder for DeliveryOption entity filtering.
 * Provides dynamic query construction for delivery options with support for filtering by
 * business ID, status, price range, and global search across delivery details.
 */
public class DeliveryOptionSpecification {

    /**
     * Builds a JPA Specification for filtering delivery options based on the provided criteria.
     * Supports filtering by business ID, multiple statuses, price range, and global search across
     * delivery option name, description, and business name.
     *
     * @param filter the filter criteria containing business ID, statuses, price range, and search parameters
     * @return a Specification for querying DeliveryOption entities
     */
    public static Specification<DeliveryOption> buildSpecification(DeliveryOptionFilterRequest filter) {
        return buildSpec(filter.getBusinessId(), filter.getStatuses(), filter.getSearch(), filter.getMinPrice(), filter.getMaxPrice());
    }

    /**
     * Builds a JPA Specification for filtering all delivery options based on the provided criteria.
     * Supports filtering by business ID, multiple statuses, price range, and global search across
     * delivery option name, description, and business name.
     *
     * @param filter the filter criteria containing business ID, statuses, price range, and search parameters
     * @return a Specification for querying DeliveryOption entities
     */
    public static Specification<DeliveryOption> buildSpecification(DeliveryOptionAllFilterRequest filter) {
        return buildSpec(filter.getBusinessId(), filter.getStatuses(), filter.getSearch(), filter.getMinPrice(), filter.getMaxPrice());
    }

    private static Specification<DeliveryOption> buildSpec(UUID businessId, List<Status> statuses, String search, BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base condition: not deleted
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            // Business ID filter
            if (businessId != null) {
                predicates.add(criteriaBuilder.equal(root.get("businessId"), businessId));
            }

            // Multiple status filter
            if (statuses != null && !statuses.isEmpty()) {
                predicates.add(root.get("status").in(statuses));
            }

            // Price range filter
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("price"), maxPrice));
            }

            // Global search filter
            if (StringUtils.hasText(search)) {
                String searchPattern = "%" + search.toLowerCase() + "%";

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
