package com.menghor.ksit.feature.master.specification;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.master.model.MajorEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

@Slf4j
public class MajorSpecification {

    /**
     * Search majors by name (partial match, case-insensitive)
     */
    public static Specification<MajorEntity> hasName(String name) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(name)) return criteriaBuilder.conjunction();
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + name.toLowerCase() + "%"
            );
        };
    }

    /**
     * Search majors by code (partial match, case-insensitive)
     */
    public static Specification<MajorEntity> hasCode(String code) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(code)) return criteriaBuilder.conjunction();
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("code")),
                    "%" + code.toLowerCase() + "%"
            );
        };
    }

    /**
     * Filter majors by status
     */
    public static Specification<MajorEntity> hasStatus(Status status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    /**
     * Filter majors by department ID
     */
    public static Specification<MajorEntity> hasDepartmentId(Long departmentId) {
        return (root, query, criteriaBuilder) -> {
            if (departmentId == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("department").get("id"), departmentId);
        };
    }

    /**
     * Filter majors by specific major ID
     */
    public static Specification<MajorEntity> hasMajorId(Long majorId) {
        return (root, query, criteriaBuilder) -> {
            if (majorId == null) {
                log.debug("Major ID is null, returning all majors");
                return criteriaBuilder.conjunction();
            }

            log.debug("Filtering majors by specific ID: {}", majorId);
            return criteriaBuilder.equal(root.get("id"), majorId);
        };
    }

    /**
     * Combined search across name and code fields
     */
    public static Specification<MajorEntity> search(String searchTerm) {
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
     * Role-based filtering specification for majors
     * This centralizes all role-based logic for majors
     */
    public static Specification<MajorEntity> forUserRole(UserEntity user) {
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
                log.debug("User has admin access, returning all majors");
                return criteriaBuilder.conjunction(); // Return all majors
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
                return criteriaBuilder.equal(root.get("department").get("id"), user.getDepartment().getId());
            }

            // Check if user is student
            boolean isStudent = user.getRoles().stream()
                    .anyMatch(role -> role.getName() == RoleEnum.STUDENT);

            if (isStudent) {
                if (user.getClasses() == null || user.getClasses().getMajor() == null) {
                    log.warn("Student {} has no class/major assigned", user.getUsername());
                    return criteriaBuilder.disjunction(); // Return no results
                }

                Long majorId = user.getClasses().getMajor().getId();
                log.debug("User is student, filtering by major ID: {}", majorId);
                return criteriaBuilder.equal(root.get("id"), majorId);
            }

            log.warn("User {} has no recognized roles", user.getUsername());
            return criteriaBuilder.disjunction(); // Return no results for unknown roles
        };
    }

    /**
     * Specification for all majors with filtering
     */
    public static Specification<MajorEntity> combine(String searchTerm, Status status, Long departmentId) {
        Specification<MajorEntity> spec = Specification.where(null);

        if (StringUtils.hasText(searchTerm)) {
            spec = spec.and(search(searchTerm));
        }

        if (status != null) {
            spec = spec.and(hasStatus(status));
        }

        if (departmentId != null) {
            spec = spec.and(hasDepartmentId(departmentId));
        }

        return spec;
    }

    /**
     * Combined specification for role-based filtering with search criteria
     */
    public static Specification<MajorEntity> combineWithUserRole(String searchTerm, Status status,
                                                                 Long departmentId, UserEntity user) {
        Specification<MajorEntity> spec = combine(searchTerm, status, departmentId);

        if (user != null) {
            spec = spec.and(forUserRole(user));
        }

        return spec;
    }
}