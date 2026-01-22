package com.emenu.features.main.specification;

import com.emenu.features.main.dto.filter.base.CategoryFilterBase;
import com.emenu.features.main.models.Category;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specification builder for Category entity filtering.
 * Provides dynamic query construction based on filter criteria including business ID, status,
 * and global search across category and business details.
 */
public class CategorySpecification {

    /**
     * Builds a JPA Specification for filtering categories based on the provided criteria.
     * Supports filtering by business ID, status, and global search across category name and business name.
     *
     * @param filter the filter criteria containing business ID, status, and search parameters
     * @return a Specification for querying Category entities
     */
    public static Specification<Category> buildSpecification(CategoryFilterBase filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base condition: not deleted
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            // Business ID filter
            if (filter.getBusinessId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("businessId"), filter.getBusinessId()));
            }

            // Status filter
            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            // Global search filter
            if (StringUtils.hasText(filter.getSearch())) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                
                Join<Object, Object> businessJoin = root.join("business", JoinType.LEFT);
                
                Predicate categoryNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), searchPattern);
                Predicate businessNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(businessJoin.get("name")), searchPattern);

                predicates.add(criteriaBuilder.or(categoryNamePredicate, businessNamePredicate));
                
                query.distinct(true);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
