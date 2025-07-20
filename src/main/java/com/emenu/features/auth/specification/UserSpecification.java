package com.emenu.features.auth.specification;

import com.emenu.features.auth.dto.filter.UserFilterRequest;
import com.emenu.features.auth.models.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> buildSpecification(UserFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base condition: not deleted
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            // Email filter
            if (StringUtils.hasText(filter.getEmail())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")),
                        "%" + filter.getEmail().toLowerCase() + "%"
                ));
            }

            // First name filter
            if (StringUtils.hasText(filter.getFirstName())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("firstName")),
                        "%" + filter.getFirstName().toLowerCase() + "%"
                ));
            }

            // Last name filter
            if (StringUtils.hasText(filter.getLastName())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("lastName")),
                        "%" + filter.getLastName().toLowerCase() + "%"
                ));
            }

            // Phone number filter
            if (StringUtils.hasText(filter.getPhoneNumber())) {
                predicates.add(criteriaBuilder.like(
                        root.get("phoneNumber"),
                        "%" + filter.getPhoneNumber() + "%"
                ));
            }

            // User type filter
            if (filter.getUserType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("userType"), filter.getUserType()));
            }

            // Account status filter
            if (filter.getAccountStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("accountStatus"), filter.getAccountStatus()));
            }

            // Business ID filter
            if (filter.getBusinessId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("businessId"), filter.getBusinessId()));
            }

            // Role filter
            if (filter.getRole() != null) {
                Join<Object, Object> roleJoin = root.join("roles", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(roleJoin.get("name"), filter.getRole()));
            }

            // Position filter
            if (StringUtils.hasText(filter.getPosition())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("position")),
                        "%" + filter.getPosition().toLowerCase() + "%"
                ));
            }

            // Global search filter
            if (StringUtils.hasText(filter.getSearch())) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                Predicate emailPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")), searchPattern);
                Predicate firstNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("firstName")), searchPattern);
                Predicate lastNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("lastName")), searchPattern);
                Predicate phonePredicate = criteriaBuilder.like(
                        root.get("phoneNumber"), searchPattern);

                predicates.add(criteriaBuilder.or(
                        emailPredicate, firstNamePredicate, lastNamePredicate, phonePredicate
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
