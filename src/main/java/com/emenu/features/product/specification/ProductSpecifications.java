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