package com.emenu.features.product.specification;

import com.emenu.features.product.dto.filter.ProductFilterRequest;
import com.emenu.features.product.models.Product;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> buildSpecification(ProductFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base condition: not deleted
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            // Business ID filter
            if (filter.getBusinessId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("businessId"), filter.getBusinessId()));
            }

            // Category filter
            if (filter.getCategoryId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("categoryId"), filter.getCategoryId()));
            }

            // Brand filter
            if (filter.getBrandId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("brandId"), filter.getBrandId()));
            }

            // Status filter
            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            // Promotion filter
            if (filter.getHasPromotion() != null) {
                Join<Object, Object> sizesJoin = root.join("sizes", JoinType.LEFT);
                if (filter.getHasPromotion()) {
                    predicates.add(criteriaBuilder.equal(sizesJoin.get("hasPromotion"), true));
                } else {
                    predicates.add(criteriaBuilder.or(
                        criteriaBuilder.equal(sizesJoin.get("hasPromotion"), false),
                        criteriaBuilder.isNull(sizesJoin.get("hasPromotion"))
                    ));
                }
                query.distinct(true);
            }

            // Price range filter
            if (filter.getMinPrice() != null || filter.getMaxPrice() != null) {
                Join<Object, Object> sizesJoin = root.join("sizes", JoinType.LEFT);
                
                if (filter.getMinPrice() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        sizesJoin.get("finalPrice"), filter.getMinPrice()));
                }
                
                if (filter.getMaxPrice() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        sizesJoin.get("finalPrice"), filter.getMaxPrice()));
                }
                
                query.distinct(true);
            }

            // Global search filter
            if (StringUtils.hasText(filter.getSearch())) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                
                Join<Object, Object> categoryJoin = root.join("category", JoinType.LEFT);
                Join<Object, Object> brandJoin = root.join("brand", JoinType.LEFT);
                Join<Object, Object> businessJoin = root.join("business", JoinType.LEFT);
                
                Predicate productNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), searchPattern);
                Predicate descriptionPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), searchPattern);
                Predicate categoryNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(categoryJoin.get("name")), searchPattern);
                Predicate brandNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(brandJoin.get("name")), searchPattern);
                Predicate businessNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(businessJoin.get("name")), searchPattern);

                predicates.add(criteriaBuilder.or(
                        productNamePredicate, descriptionPredicate, categoryNamePredicate,
                        brandNamePredicate, businessNamePredicate
                ));
                
                query.distinct(true);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}