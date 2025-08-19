package com.emenu.features.product.specification;

import com.emenu.features.product.dto.filter.ProductFilterDto;
import com.emenu.features.product.models.Product;
import com.emenu.features.product.models.ProductSize;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecifications {

    /**
     * 🚀 MAIN SPECIFICATION - Optimized for database indexes with relationship support
     * All filters use the indexes we created in the Product entity
     */
    public static Specification<Product> withFilter(ProductFilterDto filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // ✅ BASE CONDITION - Always include for security
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            // ✅ BUSINESS FILTER - Uses idx_products_business_status_deleted
            if (filter.getBusinessId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("businessId"), filter.getBusinessId()));
            }

            // ✅ STATUS FILTER - Uses idx_products_business_status_deleted or idx_products_status_created_deleted
            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            // ✅ CATEGORY FILTER - Uses idx_products_business_category_deleted or idx_products_category_created_deleted
            if (filter.getCategoryId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("categoryId"), filter.getCategoryId()));
            }

            // ✅ BRAND FILTER - Uses idx_products_business_brand_deleted or idx_products_brand_created_deleted
            if (filter.getBrandId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("brandId"), filter.getBrandId()));
            }

            // ✅ PRICE RANGE FILTER - Uses idx_products_price_deleted
            if (filter.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));
            }
            if (filter.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));
            }

            // ✅ PROMOTION FILTER - Efficient subquery approach
            if (filter.getHasPromotion() != null) {
                if (filter.getHasPromotion()) {
                    // Has promotion: Check product-level OR size-level promotions
                    Predicate productPromotion = criteriaBuilder.and(
                        criteriaBuilder.isNotNull(root.get("promotionType")),
                        criteriaBuilder.isNotNull(root.get("promotionValue")),
                        criteriaBuilder.or(
                            criteriaBuilder.isNull(root.get("promotionFromDate")),
                            criteriaBuilder.lessThanOrEqualTo(root.get("promotionFromDate"), criteriaBuilder.currentTimestamp())
                        ),
                        criteriaBuilder.or(
                            criteriaBuilder.isNull(root.get("promotionToDate")),
                            criteriaBuilder.greaterThanOrEqualTo(root.get("promotionToDate"), criteriaBuilder.currentTimestamp())
                        )
                    );
                    
                    // Check if any sizes have promotions - Uses idx_product_sizes_product_promotion_deleted
                    Subquery<Long> sizePromotionSubquery = query.subquery(Long.class);
                    Root<ProductSize> sizeRoot = sizePromotionSubquery.from(ProductSize.class);
                    sizePromotionSubquery.select(criteriaBuilder.count(sizeRoot));
                    sizePromotionSubquery.where(
                        criteriaBuilder.equal(sizeRoot.get("productId"), root.get("id")),
                        criteriaBuilder.equal(sizeRoot.get("isDeleted"), false),
                        criteriaBuilder.isNotNull(sizeRoot.get("promotionType")),
                        criteriaBuilder.isNotNull(sizeRoot.get("promotionValue")),
                        criteriaBuilder.or(
                            criteriaBuilder.isNull(sizeRoot.get("promotionFromDate")),
                            criteriaBuilder.lessThanOrEqualTo(sizeRoot.get("promotionFromDate"), criteriaBuilder.currentTimestamp())
                        ),
                        criteriaBuilder.or(
                            criteriaBuilder.isNull(sizeRoot.get("promotionToDate")),
                            criteriaBuilder.greaterThanOrEqualTo(sizeRoot.get("promotionToDate"), criteriaBuilder.currentTimestamp())
                        )
                    );
                    
                    Predicate sizePromotion = criteriaBuilder.greaterThan(sizePromotionSubquery, 0L);
                    predicates.add(criteriaBuilder.or(productPromotion, sizePromotion));
                } else {
                    // No promotion: Neither product-level NOR size-level promotions
                    Predicate noProductPromotion = criteriaBuilder.or(
                        criteriaBuilder.isNull(root.get("promotionType")),
                        criteriaBuilder.isNull(root.get("promotionValue")),
                        criteriaBuilder.greaterThan(root.get("promotionFromDate"), criteriaBuilder.currentTimestamp()),
                        criteriaBuilder.lessThan(root.get("promotionToDate"), criteriaBuilder.currentTimestamp())
                    );
                    
                    Subquery<Long> noSizePromotionSubquery = query.subquery(Long.class);
                    Root<ProductSize> sizeRoot = noSizePromotionSubquery.from(ProductSize.class);
                    noSizePromotionSubquery.select(criteriaBuilder.count(sizeRoot));
                    noSizePromotionSubquery.where(
                        criteriaBuilder.equal(sizeRoot.get("productId"), root.get("id")),
                        criteriaBuilder.equal(sizeRoot.get("isDeleted"), false),
                        criteriaBuilder.isNotNull(sizeRoot.get("promotionType")),
                        criteriaBuilder.isNotNull(sizeRoot.get("promotionValue")),
                        criteriaBuilder.or(
                            criteriaBuilder.isNull(sizeRoot.get("promotionFromDate")),
                            criteriaBuilder.lessThanOrEqualTo(sizeRoot.get("promotionFromDate"), criteriaBuilder.currentTimestamp())
                        ),
                        criteriaBuilder.or(
                            criteriaBuilder.isNull(sizeRoot.get("promotionToDate")),
                            criteriaBuilder.greaterThanOrEqualTo(sizeRoot.get("promotionToDate"), criteriaBuilder.currentTimestamp())
                        )
                    );
                    
                    Predicate noSizePromotion = criteriaBuilder.equal(noSizePromotionSubquery, 0L);
                    predicates.add(criteriaBuilder.and(noProductPromotion, noSizePromotion));
                }
            }

            // ✅ SEARCH FILTER - Uses idx_products_name_deleted and JOIN FETCH for relationships
            if (StringUtils.hasText(filter.getSearch())) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                
                // ✅ FIXED: Enhanced search with relationship joins
                Join<Object, Object> businessJoin = root.join("business", JoinType.LEFT);
                Join<Object, Object> categoryJoin = root.join("category", JoinType.LEFT);
                Join<Object, Object> brandJoin = root.join("brand", JoinType.LEFT);
                
                Predicate productNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), searchPattern);
                
                Predicate businessNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(businessJoin.get("name")), searchPattern);
                
                Predicate categoryNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(categoryJoin.get("name")), searchPattern);
                
                Predicate brandNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(brandJoin.get("name")), searchPattern);

                predicates.add(criteriaBuilder.or(
                        productNamePredicate,
                        businessNamePredicate,
                        categoryNamePredicate,
                        brandNamePredicate
                ));
                
                // Ensure distinct results when using JOINs
                query.distinct(true);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * ✅ UTILITY: Get products with active status
     */
    public static Specification<Product> hasActiveStatus() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
            criteriaBuilder.equal(root.get("status"), com.emenu.enums.product.ProductStatus.ACTIVE),
            criteriaBuilder.equal(root.get("isDeleted"), false)
        );
    }

    /**
     * ✅ UTILITY: Get products by business with relationships
     */
    public static Specification<Product> byBusinessWithRelationships(java.util.UUID businessId) {
        return (root, query, criteriaBuilder) -> {
            // Fetch relationships for better performance
            if (query.getResultType().equals(Product.class)) {
                root.fetch("business", JoinType.LEFT);
                root.fetch("category", JoinType.LEFT);
                root.fetch("brand", JoinType.LEFT);
            }
            
            return criteriaBuilder.and(
                criteriaBuilder.equal(root.get("businessId"), businessId),
                criteriaBuilder.equal(root.get("isDeleted"), false)
            );
        };
    }

    /**
     * ✅ UTILITY: Get products with promotions
     */
    public static Specification<Product> hasActivePromotions() {
        return (root, query, criteriaBuilder) -> {
            Predicate productPromotion = criteriaBuilder.and(
                criteriaBuilder.isNotNull(root.get("promotionType")),
                criteriaBuilder.isNotNull(root.get("promotionValue")),
                criteriaBuilder.or(
                    criteriaBuilder.isNull(root.get("promotionFromDate")),
                    criteriaBuilder.lessThanOrEqualTo(root.get("promotionFromDate"), criteriaBuilder.currentTimestamp())
                ),
                criteriaBuilder.or(
                    criteriaBuilder.isNull(root.get("promotionToDate")),
                    criteriaBuilder.greaterThanOrEqualTo(root.get("promotionToDate"), criteriaBuilder.currentTimestamp())
                )
            );

            return criteriaBuilder.and(
                productPromotion,
                criteriaBuilder.equal(root.get("isDeleted"), false)
            );
        };
    }
}