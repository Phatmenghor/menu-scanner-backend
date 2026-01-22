package com.emenu.features.order.specification;

import com.emenu.features.order.dto.filter.BusinessExchangeRateFilterRequest;
import com.emenu.features.order.models.BusinessExchangeRate;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specification builder for BusinessExchangeRate entity filtering.
 * Provides dynamic query construction for business-specific exchange rates with support for
 * filtering by business ID, active status, and global search across business and rate details.
 */
public class BusinessExchangeRateSpecification {

    /**
     * Builds a JPA Specification for filtering business exchange rates based on the provided criteria.
     * Supports filtering by business ID, active status, and global search across business name and notes.
     *
     * @param filter the filter criteria containing business ID, active status, and search parameters
     * @return a Specification for querying BusinessExchangeRate entities
     */
    public static Specification<BusinessExchangeRate> buildSpecification(BusinessExchangeRateFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base condition: not deleted
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            // Business ID filter
            if (filter.getBusinessId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("businessId"), filter.getBusinessId()));
            }

            // Active status filter
            if (filter.getIsActive() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), filter.getIsActive()));
            }

            // Global search filter (searches in business name and notes)
            if (StringUtils.hasText(filter.getSearch())) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                
                Join<Object, Object> businessJoin = root.join("business", JoinType.LEFT);
                
                Predicate businessNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(businessJoin.get("name")), searchPattern);
                Predicate notesPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("notes")), searchPattern);

                predicates.add(criteriaBuilder.or(businessNamePredicate, notesPredicate));
                query.distinct(true);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Creates a Specification to filter exchange rates by business ID.
     * Only returns non-deleted rates for the specified business.
     *
     * @param businessId the UUID of the business to filter by
     * @return a Specification for querying BusinessExchangeRate entities by business
     */
    public static Specification<BusinessExchangeRate> byBusiness(java.util.UUID businessId) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("businessId"), businessId)
            );
    }

    /**
     * Creates a Specification to filter for active exchange rates only.
     * Returns non-deleted rates that are marked as active.
     *
     * @return a Specification for querying active BusinessExchangeRate entities
     */
    public static Specification<BusinessExchangeRate> activeRates() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("isActive"), true)
            );
    }

    /**
     * Creates a Specification to filter exchange rates that support multiple currencies.
     * Returns non-deleted rates that have at least one valid currency conversion rate (CNY, THB, or VND).
     *
     * @return a Specification for querying BusinessExchangeRate entities with multiple currencies
     */
    public static Specification<BusinessExchangeRate> withMultipleCurrencies() {
        return (root, query, criteriaBuilder) -> {
            Predicate hasCny = criteriaBuilder.and(
                criteriaBuilder.isNotNull(root.get("usdToCnyRate")),
                criteriaBuilder.greaterThan(root.get("usdToCnyRate"), 0.0)
            );
            Predicate hasThb = criteriaBuilder.and(
                criteriaBuilder.isNotNull(root.get("usdToThbRate")),
                criteriaBuilder.greaterThan(root.get("usdToThbRate"), 0.0)
            );
            Predicate hasVnd = criteriaBuilder.and(
                criteriaBuilder.isNotNull(root.get("usdToVndRate")),
                criteriaBuilder.greaterThan(root.get("usdToVndRate"), 0.0)
            );
            
            return criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.or(hasCny, hasThb, hasVnd)
            );
        };
    }
}