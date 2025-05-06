package com.menghor.ksit.feature.master.specification;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.master.model.DepartmentEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

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

}