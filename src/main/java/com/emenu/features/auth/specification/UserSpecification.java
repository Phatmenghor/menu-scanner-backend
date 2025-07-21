package com.emenu.features.auth.specification;

import com.emenu.enums.AccountStatus;
import com.emenu.enums.RoleEnum;
import com.emenu.enums.UserType;
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

            // ✅ NEW: Roles filter (can filter by multiple roles)
            if (request.getRoles() != null && !request.getRoles().isEmpty()) {
                Join<Object, Object> rolesJoin = root.join("roles", JoinType.INNER);
                predicates.add(rolesJoin.get("name").in(request.getRoles()));

                // Use DISTINCT to avoid duplicate users when they have multiple matching roles
                query.distinct(true);
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

    // ✅ NEW: Filter by specific role
    public static Specification<User> byRole(RoleEnum role) {
        return (root, query, criteriaBuilder) -> {
            Join<Object, Object> rolesJoin = root.join("roles", JoinType.INNER);
            return criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("isDeleted"), false),
                    criteriaBuilder.equal(rolesJoin.get("name"), role)
            );
        };
    }

    // ✅ NEW: Filter by multiple roles
    public static Specification<User> byRoles(List<RoleEnum> roles) {
        return (root, query, criteriaBuilder) -> {
            Join<Object, Object> rolesJoin = root.join("roles", JoinType.INNER);
            query.distinct(true);
            return criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("isDeleted"), false),
                    rolesJoin.get("name").in(roles)
            );
        };
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