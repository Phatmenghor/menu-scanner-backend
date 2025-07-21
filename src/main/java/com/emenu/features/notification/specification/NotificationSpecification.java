package com.emenu.features.notification.specification;

import com.emenu.enums.notification.AlertType;
import com.emenu.enums.notification.NotificationChannel;
import com.emenu.features.notification.dto.filter.NotificationFilterRequest;
import com.emenu.features.notification.models.Notification;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class NotificationSpecification {

    public static Specification<Notification> buildSpecification(NotificationFilterRequest filter) {
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

            // Alert type filter
            if (filter.getAlertType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("alertType"), filter.getAlertType()));
            }

            // Channel filter
            if (filter.getChannel() != null) {
                predicates.add(criteriaBuilder.equal(root.get("channel"), filter.getChannel()));
            }

            // Read status filter
            if (filter.getIsRead() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isRead"), filter.getIsRead()));
            }

            // Sent status filter
            if (filter.getIsSent() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isSent"), filter.getIsSent()));
            }

            // Delivery status filter
            if (StringUtils.hasText(filter.getDeliveryStatus())) {
                predicates.add(criteriaBuilder.equal(root.get("deliveryStatus"), filter.getDeliveryStatus()));
            }

            // Sent date filters
            if (filter.getSentAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("sentAt"), filter.getSentAfter()));
            }

            if (filter.getSentBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("sentAt"), filter.getSentBefore()));
            }

            // Scheduled date filters
            if (filter.getScheduledAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("scheduledAt"), filter.getScheduledAfter()));
            }

            if (filter.getScheduledBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("scheduledAt"), filter.getScheduledBefore()));
            }

            // Global search filter
            if (StringUtils.hasText(filter.getSearch())) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                Predicate titlePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")), searchPattern);
                Predicate contentPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("content")), searchPattern);

                predicates.add(criteriaBuilder.or(titlePredicate, contentPredicate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    // Specific specifications for common queries
    public static Specification<Notification> unread() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("isRead"), false)
            );
    }

    public static Specification<Notification> pending() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("isSent"), false)
            );
    }

    public static Specification<Notification> failed() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("deliveryStatus"), "FAILED")
            );
    }

    public static Specification<Notification> byRecipient(java.util.UUID recipientId) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("recipientId"), recipientId)
            );
    }

    public static Specification<Notification> byChannel(NotificationChannel channel) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("channel"), channel)
            );
    }

    public static Specification<Notification> byAlertType(AlertType alertType) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("alertType"), alertType)
            );
    }

    public static Specification<Notification> readyToSend() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("isSent"), false),
                criteriaBuilder.or(
                    criteriaBuilder.isNull(root.get("scheduledAt")),
                    criteriaBuilder.lessThanOrEqualTo(root.get("scheduledAt"), java.time.LocalDateTime.now())
                )
            );
    }
}