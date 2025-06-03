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

            // REMOVED: requestId filter
             if (filter.getRequestId() != null) {
                 predicates.add(criteriaBuilder.equal(root.get("request").get("id"), filter.getRequestId()));
             }

            // ADDED: userId filter - get all history for a specific user
            if (filter.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), filter.getUserId()));
            }

            if (StringUtils.hasText(filter.getSearch())) {
                String searchTerm = "%" + filter.getSearch().toLowerCase() + "%";
                Join<RequestHistoryEntity, UserEntity> userJoin = root.join("user", JoinType.LEFT);
                Join<RequestHistoryEntity, RequestEntity> requestJoin = root.join("request", JoinType.LEFT);

                Predicate commentPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("comment")), searchTerm);
                Predicate actionByPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("actionBy")), searchTerm);
                Predicate userUsernamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(userJoin.get("username")), searchTerm);
                Predicate userKhmerFirstNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(userJoin.get("khmerFirstName")), searchTerm);
                Predicate userKhmerLastNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(userJoin.get("khmerLastName")), searchTerm);
                Predicate userEnglishFirstNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(userJoin.get("englishFirstName")), searchTerm);
                Predicate userEnglishLastNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(userJoin.get("englishLastName")), searchTerm);
                Predicate requestTitlePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(requestJoin.get("title")), searchTerm);
                Predicate titlePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")), searchTerm);

                predicates.add(criteriaBuilder.or(
                        commentPredicate,
                        actionByPredicate,
                        userUsernamePredicate,
                        userKhmerFirstNamePredicate,
                        userKhmerLastNamePredicate,
                        userEnglishFirstNamePredicate,
                        userEnglishLastNamePredicate,
                        requestTitlePredicate,
                        titlePredicate
                ));
            }

            if(filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("toStatus"), filter.getStatus()));
            }

            if (filter.getStartDate() != null) {
                // Convert LocalDate to LocalDateTime (start of day: 00:00:00)
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"), filter.getStartDate().atStartOfDay()));
            }

            if (filter.getEndDate() != null) {
                // Convert LocalDate to LocalDateTime (end of day: 23:59:59.999999999)
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdAt"), filter.getEndDate().atTime(23, 59, 59, 999999999)));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    // REMOVED: findByRequestId method since we're not using requestId anymore
     public static Specification<RequestHistoryEntity> findByRequestId(Long requestId) {
         return (root, query, criteriaBuilder) ->
                 criteriaBuilder.equal(root.get("request").get("id"), requestId);
     }

    public static Specification<RequestHistoryEntity> findByUserId(Long userId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("user").get("id"), userId);
    }
}