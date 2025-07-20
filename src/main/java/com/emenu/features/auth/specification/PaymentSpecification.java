package com.emenu.features.auth.specification;

import com.emenu.features.auth.dto.filter.PaymentFilterRequest;
import com.emenu.features.auth.models.Payment;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class PaymentSpecification {

    public static Specification<Payment> buildSpecification(PaymentFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base condition: not deleted
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            // Business ID filter
            if (filter.getBusinessId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("businessId"), filter.getBusinessId()));
            }

            // Subscription ID filter
            if (filter.getSubscriptionId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("subscriptionId"), filter.getSubscriptionId()));
            }

            // Plan ID filter
            if (filter.getPlanId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("planId"), filter.getPlanId()));
            }

            // Payment method filter
            if (filter.getPaymentMethod() != null) {
                predicates.add(criteriaBuilder.equal(root.get("paymentMethod"), filter.getPaymentMethod()));
            }

            // Status filter
            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
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

            // Reference number filter
            if (StringUtils.hasText(filter.getReferenceNumber())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("referenceNumber")),
                        "%" + filter.getReferenceNumber().toLowerCase() + "%"
                ));
            }

            // Global search filter
            if (StringUtils.hasText(filter.getSearch())) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                Predicate refPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("referenceNumber")), searchPattern);
                Predicate notesPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("notes")), searchPattern);

                predicates.add(criteriaBuilder.or(refPredicate, notesPredicate));
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

    public static Specification<Payment> byBusiness(java.util.UUID businessId) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("businessId"), businessId)
            );
    }

    public static Specification<Payment> byPlan(java.util.UUID planId) {
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
}
