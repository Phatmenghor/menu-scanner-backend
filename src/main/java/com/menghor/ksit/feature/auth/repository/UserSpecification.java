package com.menghor.ksit.feature.auth.repository;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.auth.models.Role;
import com.menghor.ksit.feature.auth.models.UserEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class UserSpecification {
    public static Specification<UserEntity> excludeShopAdmin() {
        return (root, query, criteriaBuilder) -> {
            // Join roles to check for non-SHOP_ADMIN
            Join<UserEntity, Role> roleJoin = root.join("roles", JoinType.INNER);
            return criteriaBuilder.notEqual(roleJoin.get("name"), RoleEnum.SHOP_ADMIN);
        };
    }

    public static Specification<UserEntity> hasUsername(String username) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(username)) return criteriaBuilder.conjunction();
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), "%" + username.toLowerCase() + "%");
        };
    }

    public static Specification<UserEntity> hasStatus(Status status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<UserEntity> hasRole(RoleEnum role) {
        return (root, query, criteriaBuilder) -> {
            if (role == null) return criteriaBuilder.conjunction();

            // Join roles and check for specific role
            Join<UserEntity, Role> roleJoin = root.join("roles", JoinType.INNER);
            return criteriaBuilder.equal(roleJoin.get("name"), role);
        };
    }

    // Specification for ALL users (including SHOP_ADMIN)
    public static Specification<UserEntity> createAllRolesSpecification(String username, Status status, RoleEnum role) {
        Specification<UserEntity> spec = Specification.where(null);

        if (StringUtils.hasText(username)) {
            spec = spec.and(hasUsername(username));
        }

        if (status != null) {
            spec = spec.and(hasStatus(status));
        }

        if (role != null) {
            spec = spec.and(hasRole(role));
        }

        return spec;
    }

    // Specification excluding SHOP_ADMIN by default
    public static Specification<UserEntity> createSpecification(String username, Status status, RoleEnum role) {
        Specification<UserEntity> spec = Specification.where(excludeShopAdmin());

        if (StringUtils.hasText(username)) {
            spec = spec.and(hasUsername(username));
        }

        if (status != null) {
            spec = spec.and(hasStatus(status));
        }

        if (role != null) {
            spec = spec.and(hasRole(role));
        }

        return spec;
    }
}
