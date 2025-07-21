package com.emenu.features.notification.specification;

import com.emenu.features.notification.dto.filter.MessageThreadFilterRequest;
import com.emenu.features.notification.models.MessageThread;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class MessageThreadSpecification {

    public static Specification<MessageThread> buildSpecification(MessageThreadFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base condition: not deleted
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            // Subject filter
            if (StringUtils.hasText(filter.getSubject())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("subject")),
                        "%" + filter.getSubject().toLowerCase() + "%"
                ));
            }

            // Message type filter
            if (filter.getMessageType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("messageType"), filter.getMessageType()));
            }

            // Business ID filter
            if (filter.getBusinessId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("businessId"), filter.getBusinessId()));
            }

            // Customer ID filter
            if (filter.getCustomerId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("customerId"), filter.getCustomerId()));
            }

            // Platform user ID filter
            if (filter.getPlatformUserId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("platformUserId"), filter.getPlatformUserId()));
            }

            // System generated filter
            if (filter.getIsSystemGenerated() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isSystemGenerated"), filter.getIsSystemGenerated()));
            }

            // Priority filter
            if (filter.getPriority() != null) {
                predicates.add(criteriaBuilder.equal(root.get("priority"), filter.getPriority()));
            }

            // Closed status filter
            if (filter.getIsClosed() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isClosed"), filter.getIsClosed()));
            }

            // Last message date filters
            if (filter.getLastMessageAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("lastMessageAt"), filter.getLastMessageAfter()));
            }

            if (filter.getLastMessageBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("lastMessageAt"), filter.getLastMessageBefore()));
            }

            // Global search filter
            if (StringUtils.hasText(filter.getSearch())) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                Predicate subjectPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("subject")), searchPattern);

                predicates.add(subjectPredicate);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    // Specific specifications for common queries
    public static Specification<MessageThread> isOpen() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("isClosed"), false)
            );
    }

    public static Specification<MessageThread> isClosed() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("isClosed"), true)
            );
    }

    public static Specification<MessageThread> isSystemGenerated() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("isSystemGenerated"), true)
            );
    }

    public static Specification<MessageThread> byBusiness(java.util.UUID businessId) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("businessId"), businessId)
            );
    }

    public static Specification<MessageThread> byParticipant(java.util.UUID userId) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.or(
                    criteriaBuilder.equal(root.get("businessId"), userId),
                    criteriaBuilder.equal(root.get("customerId"), userId),
                    criteriaBuilder.equal(root.get("platformUserId"), userId)
                )
            );
    }
}