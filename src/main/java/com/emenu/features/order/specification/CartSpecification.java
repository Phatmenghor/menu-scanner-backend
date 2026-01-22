package com.emenu.features.order.specification;

import com.emenu.features.order.dto.filter.CartFilterRequest;
import com.emenu.features.order.models.Cart;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specification builder for Cart entity filtering.
 * Provides dynamic query construction for shopping carts with support for filtering by
 * user ID, business ID, and global search across user and business details.
 */
public class CartSpecification {

    /**
     * Builds a JPA Specification for filtering shopping carts based on the provided criteria.
     * Supports filtering by user ID, business ID, and global search across business name, user name, and user identifier.
     *
     * @param filter the filter criteria containing user ID, business ID, and search parameters
     * @return a Specification for querying Cart entities
     */
    public static Specification<Cart> buildSpecification(CartFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base condition: not deleted
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            // User ID filter
            if (filter.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("userId"), filter.getUserId()));
            }

            // Business ID filter
            if (filter.getBusinessId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("businessId"), filter.getBusinessId()));
            }

            // Global search filter
            if (StringUtils.hasText(filter.getSearch())) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                
                Join<Object, Object> businessJoin = root.join("business", JoinType.LEFT);
                Join<Object, Object> userJoin = root.join("user", JoinType.LEFT);
                
                Predicate businessNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(businessJoin.get("name")), searchPattern);
                Predicate userNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(userJoin.get("firstName")), searchPattern);
                Predicate userIdentifierPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(userJoin.get("userIdentifier")), searchPattern);

                predicates.add(criteriaBuilder.or(
                        businessNamePredicate, userNamePredicate, userIdentifierPredicate
                ));
                
                query.distinct(true);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}