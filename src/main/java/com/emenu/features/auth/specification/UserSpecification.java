package com.emenu.features.auth.specification;

import com.emenu.enums.AccountStatus;
import com.emenu.enums.UserType;
import com.emenu.features.auth.dto.filter.UserFilterRequest;
import com.emenu.features.auth.models.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> buildSearchSpecification(UserFilterRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base condition: not deleted
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            // Business ID filter
            if (request.getBusinessId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("businessId"), request.getBusinessId()));
            }

            // Account status filter
            if (request.getAccountStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("accountStatus"), request.getAccountStatus()));
            }

            // User type filter
            if (request.getUserType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("userType"), request.getUserType()));
            }

            // Global search filter (searches across email, firstName, lastName)
            if (StringUtils.hasText(request.getSearch())) {
                String searchPattern = "%" + request.getSearch().toLowerCase() + "%";
                Predicate emailPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")), searchPattern);
                Predicate firstNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("firstName")), searchPattern);
                Predicate lastNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("lastName")), searchPattern);

                predicates.add(criteriaBuilder.or(emailPredicate, firstNamePredicate, lastNamePredicate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }


    // Specific specifications for common queries
    public static Specification<User> isActive() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("isDeleted"), false),
                        criteriaBuilder.equal(root.get("accountStatus"), AccountStatus.ACTIVE)
                );
    }

    public static Specification<User> byUserType(UserType userType) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("isDeleted"), false),
                        criteriaBuilder.equal(root.get("userType"), userType)
                );
    }

    public static Specification<User> byBusiness(java.util.UUID businessId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("isDeleted"), false),
                        criteriaBuilder.equal(root.get("businessId"), businessId)
                );
    }

    public static Specification<User> byAccountStatus(AccountStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("isDeleted"), false),
                        criteriaBuilder.equal(root.get("accountStatus"), status)
                );
    }

    public static Specification<User> byEmailContaining(String email) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("isDeleted"), false),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")),
                                "%" + email.toLowerCase() + "%")
                );
    }

    public static Specification<User> byNameContaining(String name) {
        return (root, query, criteriaBuilder) -> {
            String searchPattern = "%" + name.toLowerCase() + "%";
            return criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("isDeleted"), false),
                    criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), searchPattern),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), searchPattern)
                    )
            );
        };
    }
}