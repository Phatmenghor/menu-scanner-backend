package com.emenu.features.payment.specification;

import com.emenu.features.payment.dto.filter.ExchangeRateFilterRequest;
import com.emenu.features.payment.models.ExchangeRate;
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

    // Common specifications for quick queries
    public static Specification<ExchangeRate> activeRates() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("isActive"), true)
            );
    }

    public static Specification<ExchangeRate> historyRates() {
        return (root, query, criteriaBuilder) -> {
            query.orderBy(criteriaBuilder.desc(root.get("createdAt")));
            return criteriaBuilder.equal(root.get("isDeleted"), false);
        };
    }
}