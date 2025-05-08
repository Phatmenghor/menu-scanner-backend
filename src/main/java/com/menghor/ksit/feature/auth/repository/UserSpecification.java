package com.menghor.ksit.feature.auth.repository;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.auth.dto.request.StaffUserFilterRequestDto;
import com.menghor.ksit.feature.auth.dto.request.StudentUserFilterRequestDto;
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

    public static Specification<UserEntity> hasClassId(Long classId) {
        return (root, query, criteriaBuilder) -> {
            if (classId == null) return criteriaBuilder.conjunction();

            Join<UserEntity, ClassEntity> classJoin = root.join("classes", JoinType.LEFT);
            return criteriaBuilder.equal(classJoin.get("id"), classId);
        };
    }

    public static Specification<UserEntity> hasDepartmentId(Long departmentId) {
        return (root, query, criteriaBuilder) -> {
            if (departmentId == null) return criteriaBuilder.conjunction();

            Join<UserEntity, DepartmentEntity> departmentJoin = root.join("department", JoinType.LEFT);
            return criteriaBuilder.equal(departmentJoin.get("id"), departmentId);
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

    public static Specification<UserEntity> isTeacherStaff() {
        return (root, query, criteriaBuilder) -> {
            Join<UserEntity, Role> roleJoin = root.join("roles", JoinType.INNER);

            return roleJoin.get("name").in(
                    List.of(RoleEnum.ADMIN, RoleEnum.TEACHER, RoleEnum.STAFF, RoleEnum.DEVELOPER)
            );
        };
    }

    public static Specification<UserEntity> isStudent() {
        return (root, query, criteriaBuilder) -> {
            Join<UserEntity, Role> roleJoin = root.join("roles", JoinType.INNER);
            return criteriaBuilder.equal(roleJoin.get("name"), RoleEnum.STUDENT);
        };
    }

    public static Specification<UserEntity> hasPosition(String position) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(position)) return criteriaBuilder.conjunction();
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("currentPosition")), "%" + position.toLowerCase() + "%");
        };
    }

    public static Specification<UserEntity> hasAcademicYear(Integer academicYear) {
        return (root, query, criteriaBuilder) -> {
            if (academicYear == null) return criteriaBuilder.conjunction();

            Join<UserEntity, ClassEntity> classJoin = root.join("classes", JoinType.LEFT);
            return criteriaBuilder.equal(classJoin.get("academyYear"), academicYear);
        };
    }

    public static Specification<UserEntity> searchByName(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(searchTerm)) return criteriaBuilder.conjunction();

            String term = "%" + searchTerm.toLowerCase() + "%";

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), term));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), term));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("khmerFirstName")), term));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("khmerLastName")), term));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("englishFirstName")), term));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("englishLastName")), term));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("phoneNumber")), term));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("staffId")), term));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("nationalId")), term));

            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }

    // Original method - kept for backward compatibility
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

    // Create specification for staff users
    public static Specification<UserEntity> createStaffSpecification(StaffUserFilterRequestDto filterDto) {
        Specification<UserEntity> spec = isTeacherStaff();

        if (StringUtils.hasText(filterDto.getSearch())) {
            spec = spec.and(searchByName(filterDto.getSearch()));
        }

        if (filterDto.getStatus() != null) {
            spec = spec.and(hasStatus(filterDto.getStatus()));
        }

        if (filterDto.getRoles() != null && !filterDto.getRoles().isEmpty()) {
            spec = spec.and(hasAnyRole(filterDto.getRoles()));
        }

        if (filterDto.getDepartmentId() != null) {
            spec = spec.and(hasDepartmentId(filterDto.getDepartmentId()));
        }

        if (StringUtils.hasText(filterDto.getPosition())) {
            spec = spec.and(hasPosition(filterDto.getPosition()));
        }

        return spec;
    }

    // Create specification for student users
    public static Specification<UserEntity> createStudentSpecification(StudentUserFilterRequestDto filterDto) {
        Specification<UserEntity> spec = isStudent();

        if (StringUtils.hasText(filterDto.getSearch())) {
            spec = spec.and(searchByName(filterDto.getSearch()));
        }

        if (filterDto.getStatus() != null) {
            spec = spec.and(hasStatus(filterDto.getStatus()));
        }

        if (filterDto.getClassId() != null) {
            spec = spec.and(hasClassId(filterDto.getClassId()));
        }

        if (filterDto.getAcademicYear() != null) {
            spec = spec.and(hasAcademicYear(filterDto.getAcademicYear()));
        }

        return spec;
    }
}