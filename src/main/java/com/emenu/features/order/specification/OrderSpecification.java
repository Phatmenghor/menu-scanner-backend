package com.emenu.features.order.specification;

import com.emenu.features.order.dto.filter.OrderFilterRequest;
import com.emenu.features.order.models.Order;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA Specification builder for Order entity filtering.
 * Provides comprehensive query construction for orders with support for filtering by
 * business ID, order status, payment details, order type, customer information, and date ranges.
 */
public class OrderSpecification {

    /**
     * Builds a JPA Specification for filtering orders based on the provided criteria.
     * Supports filtering by business ID, multiple statuses, payment method, payment status, order types,
     * customer phone, date ranges, and global search across order numbers, customer details, and business names.
     *
     * @param filter the filter criteria containing various order filtering parameters
     * @return a Specification for querying Order entities
     */
    public static Specification<Order> buildSpecification(OrderFilterRequest filter) {
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

            // Payment status filter
            if (filter.getIsPaid() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isPaid"), filter.getIsPaid()));
            }

            // POS order filter
            if (filter.getIsPosOrder() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isPosOrder"), filter.getIsPosOrder()));
            }

            // Guest order filter
            if (filter.getIsGuestOrder() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isGuestOrder"), filter.getIsGuestOrder()));
            }

            // Customer phone filter
            if (StringUtils.hasText(filter.getCustomerPhone())) {
                String phonePattern = "%" + filter.getCustomerPhone().toLowerCase() + "%";
                
                // Search in both guest phone and customer phone
                Join<Object, Object> customerJoin = root.join("customer", JoinType.LEFT);
                
                Predicate guestPhonePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("guestPhone")), phonePattern);
                Predicate customerPhonePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(customerJoin.get("phoneNumber")), phonePattern);
                
                predicates.add(criteriaBuilder.or(guestPhonePredicate, customerPhonePredicate));
                query.distinct(true);
            }

            // Date range filters
            if (filter.getCreatedFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedFrom()));
            }

            if (filter.getCreatedTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedTo()));
            }

            if (filter.getConfirmedFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("confirmedAt"), filter.getConfirmedFrom()));
            }

            if (filter.getConfirmedTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("confirmedAt"), filter.getConfirmedTo()));
            }

            // Global search filter
            if (StringUtils.hasText(filter.getSearch())) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                
                Join<Object, Object> businessJoin = root.join("business", JoinType.LEFT);
                Join<Object, Object> customerJoin = root.join("customer", JoinType.LEFT);
                
                Predicate orderNumberPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("orderNumber")), searchPattern);
                Predicate guestNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("guestName")), searchPattern);
                Predicate guestPhonePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("guestPhone")), searchPattern);
                Predicate customerNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(customerJoin.get("firstName")), searchPattern);
                Predicate businessNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(businessJoin.get("name")), searchPattern);

                predicates.add(criteriaBuilder.or(
                    orderNumberPredicate, guestNamePredicate, guestPhonePredicate,
                    customerNamePredicate, businessNamePredicate
                ));
                
                query.distinct(true);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Creates a Specification to filter orders by business ID.
     * Only returns non-deleted orders for the specified business.
     *
     * @param businessId the UUID of the business to filter by
     * @return a Specification for querying Order entities by business
     */
    public static Specification<Order> byBusiness(UUID businessId) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("businessId"), businessId)
            );
    }

    /**
     * Creates a Specification to filter for POS (Point of Sale) orders only.
     * Returns non-deleted orders that are marked as POS orders.
     *
     * @return a Specification for querying POS Order entities
     */
    public static Specification<Order> posOrders() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("isPosOrder"), true)
            );
    }

    /**
     * Creates a Specification to filter for guest orders only.
     * Returns non-deleted orders placed by guest users without authentication.
     *
     * @return a Specification for querying guest Order entities
     */
    public static Specification<Order> guestOrders() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("isGuestOrder"), true)
            );
    }

    /**
     * Creates a Specification to filter for paid orders only.
     * Returns non-deleted orders that have been fully paid.
     *
     * @return a Specification for querying paid Order entities
     */
    public static Specification<Order> paidOrders() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("isPaid"), true)
            );
    }

    /**
     * Creates a Specification to filter for unpaid orders only.
     * Returns non-deleted orders that have not been paid yet.
     *
     * @return a Specification for querying unpaid Order entities
     */
    public static Specification<Order> unpaidOrders() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("isPaid"), false)
            );
    }
}