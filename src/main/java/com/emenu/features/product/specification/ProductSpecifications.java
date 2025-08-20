package com.emenu.features.product.specification;

import com.emenu.features.product.dto.filter.ProductFilterDto;
import com.emenu.features.product.models.Product;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecifications {

    public static Specification<Product> withFilter(ProductFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base condition - uses primary index
            predicates.add(cb.equal(root.get("isDeleted"), false));

            // Business filter - most selective first (90% of queries)
            if (filter.getBusinessId() != null) {
                predicates.add(cb.equal(root.get("businessId"), filter.getBusinessId()));
            }

            // Status filter
            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            // Category filter
            if (filter.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("categoryId"), filter.getCategoryId()));
            }

            // Brand filter
            if (filter.getBrandId() != null) {
                predicates.add(cb.equal(root.get("brandId"), filter.getBrandId()));
            }

            // Price range
            if (filter.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));
            }
            if (filter.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));
            }

            // Search - only join if needed
            if (StringUtils.hasText(filter.getSearch())) {
                addSearchPredicate(root, query, cb, predicates, filter.getSearch());
            }

            // Promotion filter
            if (filter.getHasPromotion() != null) {
                addPromotionPredicate(root, cb, predicates, filter.getHasPromotion());
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void addSearchPredicate(Root<Product> root, CriteriaQuery<?> query, 
                                         CriteriaBuilder cb, List<Predicate> predicates, String search) {
        String pattern = "%" + search.toLowerCase() + "%";
        
        // Product name first (no join needed)
        Predicate productName = cb.like(cb.lower(root.get("name")), pattern);
        
        // Only join for longer searches
        if (search.length() >= 3) {
            Join<Object, Object> business = root.join("business", JoinType.LEFT);
            Join<Object, Object> category = root.join("category", JoinType.LEFT);
            Join<Object, Object> brand = root.join("brand", JoinType.LEFT);
            
            Predicate businessName = cb.like(cb.lower(business.get("name")), pattern);
            Predicate categoryName = cb.like(cb.lower(category.get("name")), pattern);
            Predicate brandName = cb.like(cb.lower(brand.get("name")), pattern);

            predicates.add(cb.or(productName, businessName, categoryName, brandName));
            query.distinct(true);
        } else {
            predicates.add(productName);
        }
    }

    private static void addPromotionPredicate(Root<Product> root, CriteriaBuilder cb, 
                                            List<Predicate> predicates, Boolean hasPromotion) {
        if (hasPromotion) {
            // Has promotion
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
            // No promotion
            predicates.add(cb.or(
                cb.isNull(root.get("promotionType")),
                cb.isNull(root.get("promotionValue")),
                cb.greaterThan(root.get("promotionFromDate"), cb.currentTimestamp()),
                cb.lessThan(root.get("promotionToDate"), cb.currentTimestamp())
            ));
        }
    }
}