package com.emenu.features.auth.specification;

import com.emenu.features.auth.dto.filter.PaymentFilterRequest;
import com.emenu.features.auth.models.Payment;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PaymentSpecification {

    public static Specification<Payment> buildSpecification(PaymentFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base condition: not deleted
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            // Business filters
            if (filter.getBusinessId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("businessId"), filter.getBusinessId()));
            }

            if (filter.getBusinessIds() != null && !filter.getBusinessIds().isEmpty()) {
                predicates.add(root.get("businessId").in(filter.getBusinessIds()));
            }

            // Subscription and Plan filters
            if (filter.getSubscriptionId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("subscriptionId"), filter.getSubscriptionId()));
            }

            if (filter.getPlanId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("planId"), filter.getPlanId()));
            }

            if (filter.getPlanIds() != null && !filter.getPlanIds().isEmpty()) {
                predicates.add(root.get("planId").in(filter.getPlanIds()));
            }

            // Payment method filters
            if (filter.getPaymentMethod() != null) {
                predicates.add(criteriaBuilder.equal(root.get("paymentMethod"), filter.getPaymentMethod()));
            }

            if (filter.getPaymentMethods() != null && !filter.getPaymentMethods().isEmpty()) {
                predicates.add(root.get("paymentMethod").in(filter.getPaymentMethods()));
            }

            // Status filters
            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
                predicates.add(root.get("status").in(filter.getStatuses()));
            }

            // Boolean status filters
            if (filter.getIsCompleted() != null && filter.getIsCompleted()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), com.emenu.enums.PaymentStatus.COMPLETED));
            }

            if (filter.getIsPending() != null && filter.getIsPending()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), com.emenu.enums.PaymentStatus.PENDING));
            }

            if (filter.getIsFailed() != null && filter.getIsFailed()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), com.emenu.enums.PaymentStatus.FAILED));
            }

            // Overdue filter
            if (filter.getIsOverdue() != null && filter.getIsOverdue()) {
                LocalDateTime now = LocalDateTime.now();
                predicates.add(criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("status"), com.emenu.enums.PaymentStatus.PENDING),
                    criteriaBuilder.lessThan(root.get("dueDate"), now)
                ));
            }

            // Amount range filters
            if (filter.getMinAmount() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), filter.getMinAmount()));
            }

            if (filter.getMaxAmount() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("amount"), filter.getMaxAmount()));
            }

            // Date range filters
            if (filter.getPaymentDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("paymentDate"), filter.getPaymentDateFrom()));
            }

            if (filter.getPaymentDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("paymentDate"), filter.getPaymentDateTo()));
            }

            if (filter.getDueDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dueDate"), filter.getDueDateFrom()));
            }

            if (filter.getDueDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dueDate"), filter.getDueDateTo()));
            }

            if (filter.getCreatedDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedDateFrom()));
            }

            if (filter.getCreatedDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedDateTo()));
            }

            // Reference number filter
            if (StringUtils.hasText(filter.getReferenceNumber())) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("referenceNumber")),
                    "%" + filter.getReferenceNumber().toLowerCase() + "%"
                ));
            }

            // External transaction ID filter
            if (StringUtils.hasText(filter.getExternalTransactionId())) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("externalTransactionId")),
                    "%" + filter.getExternalTransactionId().toLowerCase() + "%"
                ));
            }

            // Processed by filter
            if (filter.getProcessedBy() != null) {
                predicates.add(criteriaBuilder.equal(root.get("processedBy"), filter.getProcessedBy()));
            }

            // Currency filter
            if (StringUtils.hasText(filter.getCurrency())) {
                predicates.add(criteriaBuilder.equal(root.get("currency"), filter.getCurrency()));
            }

            // Global search filter
            if (StringUtils.hasText(filter.getSearch())) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                
                Join<Object, Object> businessJoin = root.join("business", JoinType.LEFT);
                Join<Object, Object> planJoin = root.join("plan", JoinType.LEFT);
                
                Predicate refPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("referenceNumber")), searchPattern);
                Predicate notesPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("notes")), searchPattern);
                Predicate businessNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(businessJoin.get("name")), searchPattern);
                Predicate planNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(planJoin.get("name")), searchPattern);
                Predicate externalIdPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("externalTransactionId")), searchPattern);

                predicates.add(criteriaBuilder.or(
                    refPredicate, notesPredicate, businessNamePredicate, 
                    planNamePredicate, externalIdPredicate
                ));
                
                query.distinct(true);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    // Specific specifications for common queries
    public static Specification<Payment> byStatus(com.emenu.enums.PaymentStatus status) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("status"), status)
            );
    }

    public static Specification<Payment> byBusiness(UUID businessId) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("businessId"), businessId)
            );
    }

    public static Specification<Payment> byPlan(UUID planId) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("planId"), planId)
            );
    }

    public static Specification<Payment> completedPayments() {
        return byStatus(com.emenu.enums.PaymentStatus.COMPLETED);
    }

    public static Specification<Payment> pendingPayments() {
        return byStatus(com.emenu.enums.PaymentStatus.PENDING);
    }

    public static Specification<Payment> overduePayments() {
        return (root, query, criteriaBuilder) -> {
            LocalDateTime now = LocalDateTime.now();
            return criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("status"), com.emenu.enums.PaymentStatus.PENDING),
                criteriaBuilder.lessThan(root.get("dueDate"), now)
            );
        };
    }

    public static Specification<Payment> paymentsByDateRange(LocalDateTime start, LocalDateTime end) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.between(root.get("paymentDate"), start, end)
            );
    }

    public static Specification<Payment> paymentsByPaymentMethod(com.emenu.enums.PaymentMethod paymentMethod) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("paymentMethod"), paymentMethod)
            );
    }
}