package com.emenu.features.auth.specification;

import com.emenu.features.auth.dto.filter.ExchangeRateFilterRequest;
import com.emenu.features.auth.models.ExchangeRate;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ExchangeRateSpecification {

    public static Specification<ExchangeRate> buildSpecification(ExchangeRateFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base condition: not deleted
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            // Business ID filter
            if (filter.getBusinessId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("businessId"), filter.getBusinessId()));
            }

            // System default filter
            if (filter.getIsSystemDefault() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isSystemDefault"), filter.getIsSystemDefault()));
            }

            // Active status filter
            if (filter.getIsActive() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), filter.getIsActive()));
            }

            // Global search filter
            if (StringUtils.hasText(filter.getSearch())) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                
                Join<Object, Object> businessJoin = root.join("business", JoinType.LEFT);
                
                Predicate notesPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("notes")), searchPattern);
                Predicate businessNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(businessJoin.get("name")), searchPattern);

                predicates.add(criteriaBuilder.or(notesPredicate, businessNamePredicate));
                
                query.distinct(true);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    // Common specifications for quick queries
    public static Specification<ExchangeRate> activeRates() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("isActive"), true)
            );
    }

    public static Specification<ExchangeRate> systemDefaultRates() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("isSystemDefault"), true)
            );
    }

    public static Specification<ExchangeRate> businessRates() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.isNotNull(root.get("businessId"))
            );
    }

    public static Specification<ExchangeRate> activeSystemDefault() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("isSystemDefault"), true),
                criteriaBuilder.equal(root.get("isActive"), true)
            );
    }

    public static Specification<ExchangeRate> byBusiness(java.util.UUID businessId) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("businessId"), businessId)
            );
    }

    public static Specification<ExchangeRate> activeByBusiness(java.util.UUID businessId) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("businessId"), businessId),
                criteriaBuilder.equal(root.get("isActive"), true)
            );
    }
}
