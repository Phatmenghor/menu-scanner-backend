package com.emenu.features.main.specification;

import com.emenu.enums.common.Status;
import com.emenu.features.main.dto.filter.BrandAllFilterRequest;
import com.emenu.features.main.dto.filter.BrandFilterRequest;
import com.emenu.features.main.models.Brand;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA Specification builder for Brand entity filtering.
 * Provides dynamic query construction based on filter criteria including business ID, status,
 * and global search across brand and business details.
 */
public class BrandSpecification {

    /**
     * Builds a JPA Specification for filtering brands based on the provided criteria.
     * Supports filtering by business ID, status, and global search across brand name, description, and business name.
     *
     * @param filter the filter criteria containing business ID, status, and search parameters
     * @return a Specification for querying Brand entities
     */
    public static Specification<Brand> buildSpecification(BrandFilterRequest filter) {
        return buildSpec(filter.getBusinessId(), filter.getStatus(), filter.getSearch());
    }

    /**
     * Builds a JPA Specification for filtering all brands based on the provided criteria.
     * Supports filtering by business ID, status, and global search across brand name, description, and business name.
     *
     * @param filter the filter criteria containing business ID, status, and search parameters
     * @return a Specification for querying Brand entities
     */
    public static Specification<Brand> buildSpecification(BrandAllFilterRequest filter) {
        return buildSpec(filter.getBusinessId(), filter.getStatus(), filter.getSearch());
    }

    private static Specification<Brand> buildSpec(UUID businessId, Status status, String search) {
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

                Predicate brandNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), searchPattern);
                Predicate descriptionPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), searchPattern);
                Predicate businessNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(businessJoin.get("name")), searchPattern);

                predicates.add(criteriaBuilder.or(
                        brandNamePredicate, descriptionPredicate, businessNamePredicate
                ));

                query.distinct(true);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
