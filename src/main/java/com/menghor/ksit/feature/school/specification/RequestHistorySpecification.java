package com.menghor.ksit.feature.school.specification;

import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.school.dto.filter.RequestHistoryFilterDto;
import com.menghor.ksit.feature.school.model.RequestEntity;
import com.menghor.ksit.feature.school.model.RequestHistoryEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class RequestHistorySpecification {

    public static Specification<RequestHistoryEntity> createSpecification(RequestHistoryFilterDto filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by specific request ID
            if (filter.getRequestId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("request").get("id"), filter.getRequestId()));
            }

            // Filter by the user who PERFORMED the action
            if (filter.getActionUserId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("actionUser").get("id"), filter.getActionUserId()));
            }

            // Filter by the user who MADE the original request (through request.user)
            if (filter.getUserId() != null) {
                Join<RequestHistoryEntity, RequestEntity> requestJoin = root.join("request", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(requestJoin.get("user").get("id"), filter.getUserId()));
            }

            // Search functionality
            if (StringUtils.hasText(filter.getSearch())) {
                String searchTerm = "%" + filter.getSearch().toLowerCase() + "%";

                // Join with action user for search
                Join<RequestHistoryEntity, UserEntity> actionUserJoin = root.join("actionUser", JoinType.LEFT);
                // Join with request for search
                Join<RequestHistoryEntity, RequestEntity> requestJoin = root.join("request", JoinType.LEFT);
                // Join with request owner through request.user
                Join<RequestEntity, UserEntity> requestOwnerJoin = requestJoin.join("user", JoinType.LEFT);

                Predicate commentPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("comment")), searchTerm);
                Predicate actionByPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("actionBy")), searchTerm);
                Predicate titlePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")), searchTerm);
                Predicate requestTitlePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(requestJoin.get("title")), searchTerm);

                // Search in action user details
                Predicate actionUserUsernamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(actionUserJoin.get("username")), searchTerm);
                Predicate actionUserKhmerFirstNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(actionUserJoin.get("khmerFirstName")), searchTerm);
                Predicate actionUserEnglishFirstNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(actionUserJoin.get("englishFirstName")), searchTerm);

                // Search in request owner details (through request.user)
                Predicate requestOwnerUsernamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(requestOwnerJoin.get("username")), searchTerm);
                Predicate requestOwnerKhmerFirstNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(requestOwnerJoin.get("khmerFirstName")), searchTerm);
                Predicate requestOwnerEnglishFirstNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(requestOwnerJoin.get("englishFirstName")), searchTerm);

                predicates.add(criteriaBuilder.or(
                        commentPredicate,
                        actionByPredicate,
                        titlePredicate,
                        requestTitlePredicate,
                        actionUserUsernamePredicate,
                        actionUserKhmerFirstNamePredicate,
                        actionUserEnglishFirstNamePredicate,
                        requestOwnerUsernamePredicate,
                        requestOwnerKhmerFirstNamePredicate,
                        requestOwnerEnglishFirstNamePredicate
                ));
            }

            // Filter by status (final status of the action)
            if(filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("toStatus"), filter.getStatus()));
            }

            // Date range filtering
            if (filter.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"), filter.getStartDate().atStartOfDay()));
            }

            if (filter.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdAt"), filter.getEndDate().atTime(23, 59, 59, 999999999)));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Find history by request ID
     */
    public static Specification<RequestHistoryEntity> findByRequestId(Long requestId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("request").get("id"), requestId);
    }

    /**
     * Find history by request owner user ID (user who made the original request)
     * Gets this through request.user.id
     */
    public static Specification<RequestHistoryEntity> findByRequestOwnerUserId(Long userId) {
        return (root, query, criteriaBuilder) -> {
            Join<RequestHistoryEntity, RequestEntity> requestJoin = root.join("request", JoinType.INNER);
            return criteriaBuilder.equal(requestJoin.get("user").get("id"), userId);
        };
    }

    /**
     * Find history by action user ID (user who performed the action)
     */
    public static Specification<RequestHistoryEntity> findByActionUserId(Long userId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("actionUser").get("id"), userId);
    }
}