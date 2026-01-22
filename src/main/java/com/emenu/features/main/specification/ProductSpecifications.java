package com.emenu.features.main.specification;

import com.emenu.features.main.dto.filter.ProductFilterDto;
import com.emenu.features.main.models.Product;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specification builder for Product entity filtering.
 * Provides dynamic query construction based on comprehensive filter criteria including business ID,
 * category, brand, price range, promotion status, and global search.
 */
public class ProductSpecifications {

    /**
     * Builds a JPA Specification for filtering products based on the provided criteria.
     * Supports filtering by business ID, status, category, brand, price range, promotion status, and product name search.
     *
     * @param filter the filter criteria containing various product filtering parameters
     * @return a Specification for querying Product entities
     */
    public static Specification<Product> withFilter(ProductFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("isDeleted"), false));

            if (filter.getBusinessId() != null) {
                predicates.add(cb.equal(root.get("businessId"), filter.getBusinessId()));
            }

            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("categoryId"), filter.getCategoryId()));
            }

            if (filter.getBrandId() != null) {
                predicates.add(cb.equal(root.get("brandId"), filter.getBrandId()));
            }

            if (filter.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));
            }
            if (filter.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));
            }

            if (StringUtils.hasText(filter.getSearch())) {
                String pattern = "%" + filter.getSearch().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), pattern));
            }

            if (filter.getHasPromotion() != null) {
                addPromotionPredicate(root, cb, predicates, filter.getHasPromotion());
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Adds promotion filtering predicate to the query based on whether products should have active promotions.
     * Validates promotion existence, values, and date ranges to ensure promotions are currently active.
     *
     * @param root the product entity root
     * @param cb the criteria builder
     * @param predicates the list of predicates to add the promotion filter to
     * @param hasPromotion true to filter products with active promotions, false to filter products without promotions
     */
    private static void addPromotionPredicate(Root<Product> root, CriteriaBuilder cb,
                                              List<Predicate> predicates, Boolean hasPromotion) {
        if (hasPromotion) {
            predicates.add(cb.and(
                    cb.isNotNull(root.get("promotionType")),
                    cb.isNotNull(root.get("promotionValue")),
                    cb.or(
                            cb.isNull(root.get("promotionFromDate")),
                            cb.lessThanOrEqualTo(root.get("promotionFromDate"), cb.currentTimestamp())
                    ),
                    cb.or(
                            cb.isNull(root.get("promotionToDate")),
                            cb.greaterThanOrEqualTo(root.get("promotionToDate"), cb.currentTimestamp())
                    )
            ));
        } else {
            predicates.add(cb.or(
                    cb.isNull(root.get("promotionType")),
                    cb.isNull(root.get("promotionValue")),
                    cb.greaterThan(root.get("promotionFromDate"), cb.currentTimestamp()),
                    cb.lessThan(root.get("promotionToDate"), cb.currentTimestamp())
            ));
        }
    }
}