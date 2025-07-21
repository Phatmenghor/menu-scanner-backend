package com.emenu.features.notification.specification;

import com.emenu.enums.notification.NotificationChannel;
import com.emenu.features.notification.dto.filter.CommunicationHistoryFilterRequest;
import com.emenu.features.notification.models.CommunicationHistory;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class CommunicationHistorySpecification {

    public static Specification<CommunicationHistory> buildSpecification(CommunicationHistoryFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base condition: not deleted
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            // Recipient ID filter
            if (filter.getRecipientId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("recipientId"), filter.getRecipientId()));
            }

            // Sender ID filter
            if (filter.getSenderId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("senderId"), filter.getSenderId()));
            }

            // Business ID filter
            if (filter.getBusinessId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("businessId"), filter.getBusinessId()));
            }

            // Channel filter
            if (filter.getChannel() != null) {
                predicates.add(criteriaBuilder.equal(root.get("channel"), filter.getChannel()));
            }

            // Status filter
            if (StringUtils.hasText(filter.getStatus())) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            // Sent date filters
            if (filter.getSentAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("sentAt"), filter.getSentAfter()));
            }

            if (filter.getSentBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("sentAt"), filter.getSentBefore()));
            }

            // Global search filter
            if (StringUtils.hasText(filter.getSearch())) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                Predicate subjectPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("subject")), searchPattern);
                Predicate contentPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("content")), searchPattern);

                predicates.add(criteriaBuilder.or(subjectPredicate, contentPredicate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    // Specific specifications for common queries
    public static Specification<CommunicationHistory> byStatus(String status) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("status"), status)
            );
    }

    public static Specification<CommunicationHistory> byChannel(NotificationChannel channel) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("channel"), channel)
            );
    }

    public static Specification<CommunicationHistory> byRecipient(java.util.UUID recipientId) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("recipientId"), recipientId)
            );
    }

    public static Specification<CommunicationHistory> byBusiness(java.util.UUID businessId) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("businessId"), businessId)
            );
    }

    public static Specification<CommunicationHistory> successful() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.in(root.get("status")).value("SENT").value("DELIVERED").value("READ")
            );
    }

    public static Specification<CommunicationHistory> failed() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("status"), "FAILED")
            );
    }

    public static Specification<CommunicationHistory> inDateRange(java.time.LocalDateTime start, java.time.LocalDateTime end) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.between(root.get("sentAt"), start, end)
            );
    }
}