package com.emenu.feature.auth.specification;

import com.emenu.enumations.Status;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class PaymentSpecification {

    // Base specification to exclude deleted records
    public static Specification<PaymentEntity> notDeleted() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.notEqual(root.get("status"), Status.DELETED);
    }

    // Combined specification using individual methods
    public static Specification<PaymentEntity> combine(String search, StudentTypePayment type, Status status, Long userId) {
        return Specification.where(notDeleted())
                .and(hasSearch(search))
                .and(hasType(type))
                .and(hasStatus(status))
                .and(hasUserId(userId));
    }

    public static Specification<PaymentEntity> hasSearch(String search) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(search)) {
                return criteriaBuilder.conjunction();
            }
            
            String searchPattern = "%" + search.toLowerCase() + "%";
            Predicate itemLike = criteriaBuilder.like(
                criteriaBuilder.lower(root.get("item")), searchPattern
            );
            Predicate commendLike = criteriaBuilder.like(
                criteriaBuilder.lower(root.get("commend")), searchPattern
            );
            
            return criteriaBuilder.or(itemLike, commendLike);
        };
    }

    public static Specification<PaymentEntity> hasType(StudentTypePayment type) {
        return (root, query, criteriaBuilder) -> {
            if (type == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("type"), type);
        };
    }

    public static Specification<PaymentEntity> hasStatus(Status status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            // Don't filter by DELETED status as we already exclude it in notDeleted()
            if (status == Status.DELETED) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<PaymentEntity> hasUserId(Long userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("user").get("id"), userId);
        };
    }

    // Additional helper methods for specific use cases
    public static Specification<PaymentEntity> findActivePayments() {
        return Specification.where(notDeleted())
                .and(hasStatus(Status.ACTIVE));
    }

    public static Specification<PaymentEntity> findPaymentsByUserAndType(Long userId, StudentTypePayment type) {
        return Specification.where(notDeleted())
                .and(hasUserId(userId))
                .and(hasType(type));
    }

    public static Specification<PaymentEntity> searchPayments(String search) {
        return Specification.where(notDeleted())
                .and(hasSearch(search));
    }
}