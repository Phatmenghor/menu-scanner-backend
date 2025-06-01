package com.menghor.ksit.feature.master.specification;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.master.model.DepartmentEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

@Slf4j
public class DepartmentSpecification {

    /**
     * Search departments by name (partial match, case-insensitive)
     */
    public static Specification<DepartmentEntity> hasName(String name) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(name)) return criteriaBuilder.conjunction();
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + name.toLowerCase() + "%"
            );
        };
    }

    /**
     * Search departments by code (partial match, case-insensitive)
     */
    public static Specification<DepartmentEntity> hasCode(String code) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(code)) return criteriaBuilder.conjunction();
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("code")),
                    "%" + code.toLowerCase() + "%"
            );
        };
    }

    /**
     * Filter departments by status
     */
    public static Specification<DepartmentEntity> hasStatus(Status status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    /**
     * Filter departments by specific department ID
     */
    public static Specification<DepartmentEntity> hasDepartmentId(Long departmentId) {
        return (root, query, criteriaBuilder) -> {
            if (departmentId == null) {
                log.debug("Department ID is null, returning all departments");
                return criteriaBuilder.conjunction();
            }

            log.debug("Filtering departments by specific ID: {}", departmentId);
            return criteriaBuilder.equal(root.get("id"), departmentId);
        };
    }

    /**
     * Combined search across name and code fields
     */
    public static Specification<DepartmentEntity> search(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(searchTerm)) return criteriaBuilder.conjunction();

            String term = "%" + searchTerm.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), term),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), term)
            );
        };
    }

    /**
     * Role-based filtering specification for departments
     * This centralizes all role-based logic for departments
     */
    public static Specification<DepartmentEntity> forUserRole(UserEntity user) {
        return (root, query, criteriaBuilder) -> {
            if (user == null) {
                log.warn("User is null in role-based specification");
                return criteriaBuilder.disjunction(); // Return no results
            }

            log.debug("Applying role-based filtering for user: {} with roles: {}",
                    user.getUsername(),
                    user.getRoles().stream().map(role -> role.getName().name()).toList());

            // Check if user has admin access (ADMIN or DEVELOPER)
            boolean hasAdminAccess = user.getRoles().stream()
                    .anyMatch(role -> role.getName() == RoleEnum.ADMIN || role.getName() == RoleEnum.DEVELOPER);

            if (hasAdminAccess) {
                log.debug("User has admin access, returning all departments");
                return criteriaBuilder.conjunction(); // Return all departments
            }

            // Check if user is teacher/staff
            boolean isTeacherOrStaff = user.getRoles().stream()
                    .anyMatch(role -> role.getName() == RoleEnum.TEACHER || role.getName() == RoleEnum.STAFF);

            if (isTeacherOrStaff) {
                if (user.getDepartment() == null) {
                    log.warn("Staff/Teacher {} has no department assigned", user.getUsername());
                    return criteriaBuilder.disjunction(); // Return no results
                }

                log.debug("User is staff/teacher, filtering by department ID: {}", user.getDepartment().getId());
                return criteriaBuilder.equal(root.get("id"), user.getDepartment().getId());
            }

            // Check if user is student
            boolean isStudent = user.getRoles().stream()
                    .anyMatch(role -> role.getName() == RoleEnum.STUDENT);

            if (isStudent) {
                if (user.getClasses() == null || user.getClasses().getMajor() == null ||
                        user.getClasses().getMajor().getDepartment() == null) {
                    log.warn("Student {} has no class/major/department assigned", user.getUsername());
                    return criteriaBuilder.disjunction(); // Return no results
                }

                Long departmentId = user.getClasses().getMajor().getDepartment().getId();
                log.debug("User is student, filtering by department ID: {}", departmentId);
                return criteriaBuilder.equal(root.get("id"), departmentId);
            }

            log.warn("User {} has no recognized roles", user.getUsername());
            return criteriaBuilder.disjunction(); // Return no results for unknown roles
        };
    }

    /**
     * Specification for all departments with filtering
     */
    public static Specification<DepartmentEntity> combine(String searchTerm, Status status) {
        Specification<DepartmentEntity> spec = Specification.where(null);

        if (StringUtils.hasText(searchTerm)) {
            spec = spec.and(search(searchTerm));
        }

        if (status != null) {
            spec = spec.and(hasStatus(status));
        }

        return spec;
    }

    /**
     * Combined specification for role-based filtering with search criteria
     */
    public static Specification<DepartmentEntity> combineWithUserRole(String searchTerm, Status status, UserEntity user) {
        Specification<DepartmentEntity> spec = combine(searchTerm, status);

        if (user != null) {
            spec = spec.and(forUserRole(user));
        }

        return spec;
    }
}