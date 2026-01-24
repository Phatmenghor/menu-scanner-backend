package com.emenu.features.main.specification;

import com.emenu.enums.common.Status;
import com.emenu.features.main.dto.filter.CategoryAllFilterRequest;
import com.emenu.features.main.dto.filter.CategoryFilterRequest;
import com.emenu.features.main.models.Category;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    public static Specification<Category> buildSpecification(CategoryFilterRequest filter) {
        return buildSpec(filter.getBusinessId(), filter.getStatus(), filter.getSearch());
    }

    /**
     * Builds a JPA Specification for filtering all categories based on the provided criteria.
     * Supports filtering by business ID, status, and global search across category name and business name.
     *
     * @param filter the filter criteria containing business ID, status, and search parameters
     * @return a Specification for querying Category entities
     */
    public static Specification<Category> buildSpecification(CategoryAllFilterRequest filter) {
        return buildSpec(filter.getBusinessId(), filter.getStatus(), filter.getSearch());
    }

    private static Specification<Category> buildSpec(UUID businessId, Status status, String search) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base condition: not deleted
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            // Business ID filter
            if (businessId != null) {
                predicates.add(criteriaBuilder.equal(root.get("businessId"), businessId));
            }

            // Status filter
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // Global search filter
            if (StringUtils.hasText(search)) {
                String searchPattern = "%" + search.toLowerCase() + "%";

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
