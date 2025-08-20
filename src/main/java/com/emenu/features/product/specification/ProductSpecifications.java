package com.emenu.features.product.specification;

import com.emenu.features.product.dto.filter.ProductFilterDto;
import com.emenu.features.product.models.Product;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

            // Category filter - use categoryId directly (no JOIN)
            if (filter.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("categoryId"), filter.getCategoryId()));
            }

            // Brand filter - use brandId directly (no JOIN)
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

            // ✅ OPTIMIZED SEARCH: Only search product name (no expensive JOINs)
            if (StringUtils.hasText(filter.getSearch())) {
                addOptimizedSearchPredicate(root, cb, predicates, filter.getSearch());
            }

            // Promotion filter
            if (filter.getHasPromotion() != null) {
                addPromotionPredicate(root, cb, predicates, filter.getHasPromotion());
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * ✅ OPTIMIZED SEARCH: Only search product name to avoid expensive JOINs
     * For relationship-based searches, use separate service methods
     */
    private static void addOptimizedSearchPredicate(Root<Product> root, CriteriaBuilder cb, 
                                                   List<Predicate> predicates, String search) {
        String pattern = "%" + search.toLowerCase() + "%";
        
        // Only search product name - fast and uses index
        Predicate productName = cb.like(cb.lower(root.get("name")), pattern);
        predicates.add(productName);
    }

    /**
     * ✅ SEPARATE SPECIFICATION: For relationship-based search (when needed)
     * Use this only for advanced search functionality
     */
    public static Specification<Product> withRelationshipSearch(String search) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(search)) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("isDeleted"), false));

            String pattern = "%" + search.toLowerCase() + "%";
            
            // Product name first (no join needed)
            Predicate productName = cb.like(cb.lower(root.get("name")), pattern);
            
            // Only join for relationship search
            Join<Object, Object> business = root.join("business", JoinType.LEFT);
            Join<Object, Object> category = root.join("category", JoinType.LEFT);
            Join<Object, Object> brand = root.join("brand", JoinType.LEFT);
            
            Predicate businessName = cb.like(cb.lower(business.get("name")), pattern);
            Predicate categoryName = cb.like(cb.lower(category.get("name")), pattern);
            Predicate brandName = cb.like(cb.lower(brand.get("name")), pattern);

            predicates.add(cb.or(productName, businessName, categoryName, brandName));
            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
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

    /**
     * ✅ OPTIMIZED: Simple filters without relationships
     */
    public static Specification<Product> withSimpleFilters(UUID businessId, UUID categoryId, UUID brandId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("isDeleted"), false));

            if (businessId != null) {
                predicates.add(cb.equal(root.get("businessId"), businessId));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("categoryId"), categoryId));
            }
            if (brandId != null) {
                predicates.add(cb.equal(root.get("brandId"), brandId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}