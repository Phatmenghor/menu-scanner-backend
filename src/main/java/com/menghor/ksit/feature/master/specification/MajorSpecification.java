package com.menghor.ksit.feature.master.specification;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.master.model.MajorEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

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
}