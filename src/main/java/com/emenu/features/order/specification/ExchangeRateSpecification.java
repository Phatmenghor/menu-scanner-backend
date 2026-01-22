package com.emenu.features.order.specification;

import com.emenu.features.order.dto.filter.ExchangeRateFilterRequest;
import com.emenu.features.order.models.ExchangeRate;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specification builder for ExchangeRate entity filtering.
 * Provides dynamic query construction for system-wide exchange rates with support for
 * filtering by active status and global search across rate notes.
 */
public class ExchangeRateSpecification {

    /**
     * Builds a JPA Specification for filtering exchange rates based on the provided criteria.
     * Supports filtering by active status and global search across notes.
     *
     * @param filter the filter criteria containing active status and search parameters
     * @return a Specification for querying ExchangeRate entities
     */
    public static Specification<ExchangeRate> buildSpecification(ExchangeRateFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base condition: not deleted
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            // Active status filter
            if (filter.getIsActive() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), filter.getIsActive()));
            }

            // Global search filter (searches in notes)
            if (StringUtils.hasText(filter.getSearch())) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                
                Predicate notesPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("notes")), searchPattern);

                predicates.add(notesPredicate);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Creates a Specification to filter for active exchange rates only.
     * Returns non-deleted rates that are marked as active.
     *
     * @return a Specification for querying active ExchangeRate entities
     */
    public static Specification<ExchangeRate> activeRates() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("isActive"), true)
            );
    }

    /**
     * Creates a Specification to retrieve exchange rate history.
     * Returns all non-deleted rates ordered by creation date in descending order (newest first).
     *
     * @return a Specification for querying historical ExchangeRate entities
     */
    public static Specification<ExchangeRate> historyRates() {
        return (root, query, criteriaBuilder) -> {
            query.orderBy(criteriaBuilder.desc(root.get("createdAt")));
            return criteriaBuilder.equal(root.get("isDeleted"), false);
        };
    }
}