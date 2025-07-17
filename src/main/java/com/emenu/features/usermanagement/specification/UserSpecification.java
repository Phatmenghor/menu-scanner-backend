package com.emenu.features.usermanagement.specification;

import com.emenu.enums.AccountStatus;
import com.emenu.enums.CustomerTier;
import com.emenu.enums.RoleEnum;
import com.emenu.enums.UserType;
import com.emenu.features.usermanagement.domain.Role;
import com.emenu.features.usermanagement.domain.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserSpecification {

    public static Specification<User> notDeleted() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("isDeleted"), false);
    }

    public static Specification<User> hasSearch(String search) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(search)) {
                return criteriaBuilder.conjunction();
            }
            
            String searchPattern = "%" + search.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();
            
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), searchPattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), searchPattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), searchPattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("phoneNumber")), searchPattern));
            
            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<User> hasUserType(UserType userType) {
        return (root, query, criteriaBuilder) -> {
            if (userType == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("userType"), userType);
        };
    }

    public static Specification<User> hasAccountStatus(AccountStatus accountStatus) {
        return (root, query, criteriaBuilder) -> {
            if (accountStatus == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("accountStatus"), accountStatus);
        };
    }

    public static Specification<User> hasRole(RoleEnum role) {
        return (root, query, criteriaBuilder) -> {
            if (role == null) {
                return criteriaBuilder.conjunction();
            }
            
            Join<User, Role> roleJoin = root.join("roles", JoinType.INNER);
            return criteriaBuilder.equal(roleJoin.get("name"), role);
        };
    }

    public static Specification<User> hasAnyRole(List<RoleEnum> roles) {
        return (root, query, criteriaBuilder) -> {
            if (roles == null || roles.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            
            Join<User, Role> roleJoin = root.join("roles", JoinType.INNER);
            return roleJoin.get("name").in(roles);
        };
    }

    public static Specification<User> hasCustomerTier(CustomerTier customerTier) {
        return (root, query, criteriaBuilder) -> {
            if (customerTier == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("customerTier"), customerTier);
        };
    }

    public static Specification<User> hasBusinessId(UUID businessId) {
        return (root, query, criteriaBuilder) -> {
            if (businessId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("businessId"), businessId);
        };
    }

    public static Specification<User> isEmailVerified(Boolean emailVerified) {
        return (root, query, criteriaBuilder) -> {
            if (emailVerified == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("emailVerified"), emailVerified);
        };
    }

    public static Specification<User> isTwoFactorEnabled(Boolean twoFactorEnabled) {
        return (root, query, criteriaBuilder) -> {
            if (twoFactorEnabled == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("twoFactorEnabled"), twoFactorEnabled);
        };
    }

    public static Specification<User> createdAfter(LocalDate date) {
        return (root, query, criteriaBuilder) -> {
            if (date == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), date.atStartOfDay());
        };
    }

    public static Specification<User> createdBefore(LocalDate date) {
        return (root, query, criteriaBuilder) -> {
            if (date == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), date.atTime(23, 59, 59));
        };
    }

    public static Specification<User> lastLoginAfter(LocalDate date) {
        return (root, query, criteriaBuilder) -> {
            if (date == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("lastLogin"), date.atStartOfDay());
        };
    }

    public static Specification<User> lastLoginBefore(LocalDate date) {
        return (root, query, criteriaBuilder) -> {
            if (date == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("lastLogin"), date.atTime(23, 59, 59));
        };
    }

    public static Specification<User> loyaltyPointsBetween(Integer minPoints, Integer maxPoints) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (minPoints != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("loyaltyPoints"), minPoints));
            }
            
            if (maxPoints != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("loyaltyPoints"), maxPoints));
            }
            
            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<User> totalSpentBetween(Double minSpent, Double maxSpent) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (minSpent != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("totalSpent"), minSpent));
            }
            
            if (maxSpent != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("totalSpent"), maxSpent));
            }
            
            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}