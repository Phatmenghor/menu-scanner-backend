package com.emenu.features.payment.specification;

import com.emenu.features.payment.dto.filter.BusinessOrderPaymentFilterRequest;
import com.emenu.features.payment.models.BusinessOrderPayment;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BusinessOrderPaymentSpecification {

    public static Specification<BusinessOrderPayment> buildSpecification(BusinessOrderPaymentFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base condition: not deleted
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            // Business ID filter
            if (filter.getBusinessId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("businessId"), filter.getBusinessId()));
            }

            // Multiple status filter
            if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
                predicates.add(root.get("status").in(filter.getStatuses()));
            }

            // Payment method filter
            if (filter.getPaymentMethod() != null) {
                predicates.add(criteriaBuilder.equal(root.get("paymentMethod"), filter.getPaymentMethod()));
            }

            // Customer payment method filter
            if (StringUtils.hasText(filter.getCustomerPaymentMethod())) {
                String methodPattern = "%" + filter.getCustomerPaymentMethod().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("customerPaymentMethod")), methodPattern));
            }

            // Customer phone filter
            if (StringUtils.hasText(filter.getCustomerPhone())) {
                String phonePattern = "%" + filter.getCustomerPhone().toLowerCase() + "%";
                
                Join<Object, Object> orderJoin = root.join("order", JoinType.LEFT);
                Join<Object, Object> customerJoin = orderJoin.join("customer", JoinType.LEFT);
                
                Predicate guestPhonePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(orderJoin.get("guestPhone")), phonePattern);
                Predicate customerPhonePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(customerJoin.get("phoneNumber")), phonePattern);
                
                predicates.add(criteriaBuilder.or(guestPhonePredicate, customerPhonePredicate));
                query.distinct(true);
            }

            // POS order filter
            if (filter.getIsPosOrder() != null) {
                Join<Object, Object> orderJoin = root.join("order", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(orderJoin.get("isPosOrder"), filter.getIsPosOrder()));
            }

            // Guest order filter
            if (filter.getIsGuestOrder() != null) {
                Join<Object, Object> orderJoin = root.join("order", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(orderJoin.get("isGuestOrder"), filter.getIsGuestOrder()));
            }

            // Date range filters
            if (filter.getCreatedFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedFrom()));
            }

            if (filter.getCreatedTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedTo()));
            }

            // Global search filter
            if (StringUtils.hasText(filter.getSearch())) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                
                Join<Object, Object> businessJoin = root.join("business", JoinType.LEFT);
                Join<Object, Object> orderJoin = root.join("order", JoinType.LEFT);
                Join<Object, Object> customerJoin = orderJoin.join("customer", JoinType.LEFT);
                
                Predicate referencePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("paymentReference")), searchPattern);
                Predicate businessNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(businessJoin.get("name")), searchPattern);
                Predicate orderNumberPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(orderJoin.get("orderNumber")), searchPattern);
                Predicate guestNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(orderJoin.get("guestName")), searchPattern);
                Predicate customerNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(customerJoin.get("firstName")), searchPattern);

                predicates.add(criteriaBuilder.or(
                    referencePredicate, businessNamePredicate, orderNumberPredicate,
                    guestNamePredicate, customerNamePredicate
                ));
                
                query.distinct(true);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    // Common specifications for quick queries
    public static Specification<BusinessOrderPayment> byBusiness(UUID businessId) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("businessId"), businessId)
            );
    }

    public static Specification<BusinessOrderPayment> completedPayments() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("status"), com.emenu.enums.payment.PaymentStatus.COMPLETED)
            );
    }

    public static Specification<BusinessOrderPayment> posPayments() {
        return (root, query, criteriaBuilder) -> {
            Join<Object, Object> orderJoin = root.join("order", JoinType.LEFT);
            return criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(orderJoin.get("isPosOrder"), true)
            );
        };
    }

    public static Specification<BusinessOrderPayment> guestPayments() {
        return (root, query, criteriaBuilder) -> {
            Join<Object, Object> orderJoin = root.join("order", JoinType.LEFT);
            return criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(orderJoin.get("isGuestOrder"), true)
            );
        };
    }
}