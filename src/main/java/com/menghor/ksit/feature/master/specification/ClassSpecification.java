package com.menghor.ksit.feature.master.specification;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.master.model.ClassEntity;
import com.menghor.ksit.feature.master.model.DepartmentEntity;
import com.menghor.ksit.feature.master.model.MajorEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

@Slf4j
public class ClassSpecification {

    public static Specification<ClassEntity> hasCode(String code) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(code)) return criteriaBuilder.conjunction();
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), "%" + code.toLowerCase() + "%");
        };
    }

    public static Specification<ClassEntity> hasAcademyYear(Integer academyYear) {
        return (root, query, criteriaBuilder) -> {
            if (academyYear == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("academyYear"), academyYear);
        };
    }

    public static Specification<ClassEntity> hasStatus(Status status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    /**
     * Filter class by Major ID
     */
    public static Specification<ClassEntity> hasMajorId(Long majorId) {
        return (root, query, criteriaBuilder) -> {
            if (majorId == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("major").get("id"), majorId);
        };
    }

    /**
     * Filter classes by department ID (through major -> department relationship)
     */
    public static Specification<ClassEntity> hasDepartmentId(Long departmentId) {
        return (root, query, criteriaBuilder) -> {
            if (departmentId == null) {
                log.debug("Department ID is null, returning all classes");
                return criteriaBuilder.conjunction();
            }

            log.debug("Filtering classes by department ID: {}", departmentId);
            Join<ClassEntity, MajorEntity> majorJoin = root.join("major", JoinType.INNER);
            Join<MajorEntity, DepartmentEntity> departmentJoin = majorJoin.join("department", JoinType.INNER);
            return criteriaBuilder.equal(departmentJoin.get("id"), departmentId);
        };
    }

    /**
     * Filter classes for a specific student (by class ID)
     */
    public static Specification<ClassEntity> hasStudentClassId(Long classId) {
        return (root, query, criteriaBuilder) -> {
            if (classId == null) {
                log.debug("Class ID is null, returning no classes for student");
                return criteriaBuilder.disjunction(); // Returns false - no classes
            }

            log.debug("Filtering classes for student's class ID: {}", classId);
            return criteriaBuilder.equal(root.get("id"), classId);
        };
    }

    // Combined search across code field
    public static Specification<ClassEntity> search(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(searchTerm)) return criteriaBuilder.conjunction();

            String term = "%" + searchTerm.toLowerCase() + "%";
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), term);
        };
    }

    /**
     * Role-based filtering specification
     * This centralizes all role-based logic for classes
     */
    public static Specification<ClassEntity> forUserRole(UserEntity user) {
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
                log.debug("User has admin access, returning all classes");
                return criteriaBuilder.conjunction(); // Return all classes
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
                Join<ClassEntity, MajorEntity> majorJoin = root.join("major", JoinType.INNER);
                Join<MajorEntity, DepartmentEntity> departmentJoin = majorJoin.join("department", JoinType.INNER);
                return criteriaBuilder.equal(departmentJoin.get("id"), user.getDepartment().getId());
            }

            // Check if user is student
            boolean isStudent = user.getRoles().stream()
                    .anyMatch(role -> role.getName() == RoleEnum.STUDENT);

            if (isStudent) {
                if (user.getClasses() == null) {
                    log.warn("Student {} has no class assigned", user.getUsername());
                    return criteriaBuilder.disjunction(); // Return no results
                }

                log.debug("User is student, filtering by class ID: {}", user.getClasses().getId());
                return criteriaBuilder.equal(root.get("id"), user.getClasses().getId());
            }

            log.warn("User {} has no recognized roles", user.getUsername());
            return criteriaBuilder.disjunction(); // Return no results for unknown roles
        };
    }

    // Specification for combining all filters
    public static Specification<ClassEntity> combine(String searchTerm, Integer academyYear, Status status, Long majorId) {
        Specification<ClassEntity> spec = Specification.where(null);

        if (StringUtils.hasText(searchTerm)) {
            spec = spec.and(search(searchTerm));
        }

        if (academyYear != null) {
            spec = spec.and(hasAcademyYear(academyYear));
        }

        if (status != null) {
            spec = spec.and(hasStatus(status));
        }

        if (majorId != null) {
            spec = spec.and(hasMajorId(majorId));
        }

        return spec;
    }

    /**
     * Combined specification for role-based filtering with search criteria
     */
    public static Specification<ClassEntity> combineWithUserRole(String searchTerm, Integer academyYear,
                                                                 Status status, Long majorId, UserEntity user) {
        Specification<ClassEntity> spec = combine(searchTerm, academyYear, status, majorId);

        if (user != null) {
            spec = spec.and(forUserRole(user));
        }

        return spec;
    }
}