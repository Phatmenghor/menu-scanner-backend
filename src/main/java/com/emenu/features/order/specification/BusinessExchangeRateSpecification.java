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

public class BusinessExchangeRateSpecification {

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

    // Common specifications for quick queries
    public static Specification<BusinessExchangeRate> byBusiness(java.util.UUID businessId) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("businessId"), businessId)
            );
    }

    public static Specification<BusinessExchangeRate> activeRates() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("isActive"), true)
            );
    }

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