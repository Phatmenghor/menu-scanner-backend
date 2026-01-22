package com.emenu.features.main.specification;

import com.emenu.features.main.dto.filter.base.BannerFilterBase;
import com.emenu.features.main.models.Banner;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specification builder for Banner entity filtering.
 * Provides dynamic query construction based on filter criteria including business ID, status,
 * and global search across business details.
 */
public class BannerSpecification {

    /**
     * Builds a JPA Specification for filtering banners based on the provided criteria.
     * Supports filtering by business ID, status, and global search across business name.
     *
     * @param filter the filter criteria containing business ID, status, and search parameters
     * @return a Specification for querying Banner entities
     */
    public static Specification<Banner> buildSpecification(BannerFilterBase filter) {
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
                
                Predicate businessNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(businessJoin.get("name")), searchPattern);

                predicates.add(criteriaBuilder.or(businessNamePredicate));
                
                query.distinct(true);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}