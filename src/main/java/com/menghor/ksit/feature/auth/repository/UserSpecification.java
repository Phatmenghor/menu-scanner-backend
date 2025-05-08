package com.menghor.ksit.feature.auth.repository;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.auth.models.Role;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.master.model.ClassEntity;
import com.menghor.ksit.feature.master.model.DepartmentEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

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

    // Add this method to UserSpecification class
    public static Specification<UserEntity> hasClassId(Long classId) {
        return (root, query, criteriaBuilder) -> {
            if (classId == null) return criteriaBuilder.conjunction();

            Join<UserEntity, ClassEntity> classJoin = root.join("classes", JoinType.LEFT);
            return criteriaBuilder.equal(classJoin.get("id"), classId);
        };
    }

    public static Specification<UserEntity> hasAnyRole(List<RoleEnum> roles) {
        return (root, query, criteriaBuilder) -> {
            if (roles == null || roles.isEmpty()) return criteriaBuilder.conjunction();

            // Join roles and check for any role in the list
            Join<UserEntity, Role> roleJoin = root.join("roles", JoinType.INNER);
            return roleJoin.get("name").in(roles);
        };
    }

    public static Specification<UserEntity> hasDepartment(String departmentName) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(departmentName)) return criteriaBuilder.conjunction();

            Join<UserEntity, DepartmentEntity> departmentJoin = root.join("department", JoinType.LEFT);
            return criteriaBuilder.like(
                    criteriaBuilder.lower(departmentJoin.get("name")),
                    "%" + departmentName.toLowerCase() + "%"
            );
        };
    }

    public static Specification<UserEntity> hasClass(String className) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(className)) return criteriaBuilder.conjunction();

            Join<UserEntity, ClassEntity> classJoin = root.join("classes", JoinType.LEFT);
            return criteriaBuilder.like(
                    criteriaBuilder.lower(classJoin.get("code")),
                    "%" + className.toLowerCase() + "%"
            );
        };
    }

    public static Specification<UserEntity> hasAcademicYear(Integer academicYear) {
        return (root, query, criteriaBuilder) -> {
            if (academicYear == null) return criteriaBuilder.conjunction();

            Join<UserEntity, ClassEntity> classJoin = root.join("classes", JoinType.LEFT);
            return criteriaBuilder.equal(classJoin.get("academyYear"), academicYear);
        };
    }

    // Combined search across multiple fields
    public static Specification<UserEntity> searchByName(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(searchTerm)) return criteriaBuilder.conjunction();

            String term = "%" + searchTerm.toLowerCase() + "%";

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), term));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("khmerFirstName")), term));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("khmerLastName")), term));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("englishFirstName")), term));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("englishLastName")), term));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("phoneNumber")), term));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("studentId")), term));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("staffId")), term));

            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }

    // Then update the createAdvancedSpecification method to include it
    public static Specification<UserEntity> createAdvancedSpecification(
            String searchTerm,
            Status status,
            List<RoleEnum> roles) {

        Specification<UserEntity> spec = Specification.where(null);

        if (StringUtils.hasText(searchTerm)) {
            spec = spec.and(searchByName(searchTerm));
        }

        if (status != null) {
            spec = spec.and(hasStatus(status));
        }


        if (roles != null && !roles.isEmpty()) {
            spec = spec.and(hasAnyRole(roles));
        }


        return spec;
    }
}