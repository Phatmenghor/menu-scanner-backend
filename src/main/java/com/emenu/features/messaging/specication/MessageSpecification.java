package com.emenu.features.messaging.specication;

import com.emenu.features.messaging.dto.filter.MessageFilterRequest;
import com.emenu.features.messaging.models.Message;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class MessageSpecification {

    public static Specification<Message> buildSpecification(MessageFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base condition: not deleted
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            // Sender ID filter
            if (filter.getSenderId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("senderId"), filter.getSenderId()));
            }

            // Recipient ID filter
            if (filter.getRecipientId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("recipientId"), filter.getRecipientId()));
            }

            // Sender email filter
            if (StringUtils.hasText(filter.getSenderEmail())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("senderEmail")),
                        "%" + filter.getSenderEmail().toLowerCase() + "%"
                ));
            }

            // Recipient email filter
            if (StringUtils.hasText(filter.getRecipientEmail())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("recipientEmail")),
                        "%" + filter.getRecipientEmail().toLowerCase() + "%"
                ));
            }

            // Subject filter
            if (StringUtils.hasText(filter.getSubject())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("subject")),
                        "%" + filter.getSubject().toLowerCase() + "%"
                ));
            }

            // Content filter
            if (StringUtils.hasText(filter.getContent())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("content")),
                        "%" + filter.getContent().toLowerCase() + "%"
                ));
            }

            // Message type filter
            if (filter.getMessageType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("messageType"), filter.getMessageType()));
            }

            // Status filter
            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            // Priority filter
            if (StringUtils.hasText(filter.getPriority())) {
                predicates.add(criteriaBuilder.equal(root.get("priority"), filter.getPriority()));
            }

            // Business ID filter
            if (filter.getBusinessId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("businessId"), filter.getBusinessId()));
            }

            // Read status filter
            if (filter.getIsRead() != null) {
                if (filter.getIsRead()) {
                    predicates.add(criteriaBuilder.isNotNull(root.get("readAt")));
                } else {
                    predicates.add(criteriaBuilder.isNull(root.get("readAt")));
                }
            }

            // Date range filters
            if (filter.getDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), filter.getDateFrom()));
            }

            if (filter.getDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), filter.getDateTo()));
            }

            // Global search filter
            if (StringUtils.hasText(filter.getSearch())) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                Predicate subjectPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("subject")), searchPattern);
                Predicate contentPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("content")), searchPattern);
                Predicate senderPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("senderName")), searchPattern);
                Predicate recipientPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("recipientName")), searchPattern);

                predicates.add(criteriaBuilder.or(
                        subjectPredicate, contentPredicate, senderPredicate, recipientPredicate
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Message> findByRecipientId(java.util.UUID recipientId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));
            predicates.add(criteriaBuilder.equal(root.get("recipientId"), recipientId));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Message> findBySenderId(java.util.UUID senderId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));
            predicates.add(criteriaBuilder.equal(root.get("senderId"), senderId));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Message> findUnreadByRecipientId(java.util.UUID recipientId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));
            predicates.add(criteriaBuilder.equal(root.get("recipientId"), recipientId));
            predicates.add(criteriaBuilder.isNull(root.get("readAt")));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Message> findByBusinessId(java.util.UUID businessId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));
            predicates.add(criteriaBuilder.equal(root.get("businessId"), businessId));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Message> findByMessageType(com.emenu.enums.MessageType messageType) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));
            predicates.add(criteriaBuilder.equal(root.get("messageType"), messageType));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Message> findByPriority(String priority) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));
            predicates.add(criteriaBuilder.equal(root.get("priority"), priority));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Message> searchInContent(String query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));
            
            String searchPattern = "%" + query.toLowerCase() + "%";
            Predicate subjectPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("subject")), searchPattern);
            Predicate contentPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("content")), searchPattern);
            
            predicates.add(criteriaBuilder.or(subjectPredicate, contentPredicate));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}