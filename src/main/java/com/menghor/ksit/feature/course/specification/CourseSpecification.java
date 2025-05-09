package com.menghor.ksit.feature.course.specification;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.course.model.CourseEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class CourseSpecification {

    /**
     * Search courses by code (partial match, case-insensitive)
     */
    public static Specification<CourseEntity> hasCode(String code) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(code)) return criteriaBuilder.conjunction();
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("code")),
                "%" + code.toLowerCase() + "%"
            );
        };
    }

    /**
     * Search courses by Khmer name (partial match, case-insensitive)
     */
    public static Specification<CourseEntity> hasNameKH(String nameKH) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(nameKH)) return criteriaBuilder.conjunction();
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("nameKH")),
                "%" + nameKH.toLowerCase() + "%"
            );
        };
    }

    /**
     * Search courses by English name (partial match, case-insensitive)
     */
    public static Specification<CourseEntity> hasNameEn(String nameEn) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(nameEn)) return criteriaBuilder.conjunction();
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("nameEn")),
                "%" + nameEn.toLowerCase() + "%"
            );
        };
    }

    /**
     * Filter courses by status
     */
    public static Specification<CourseEntity> hasStatus(Status status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }
    
    /**
     * Combined search across code, nameKH, and nameEn fields
     */
    public static Specification<CourseEntity> search(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(searchTerm)) return criteriaBuilder.conjunction();

            String term = "%" + searchTerm.toLowerCase() + "%";
            return criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), term),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("nameKH")), term),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("nameEn")), term)
            );
        };
    }
    
    /**
     * Specification for all courses with filtering
     */
    public static Specification<CourseEntity> combine(String searchTerm, Status status) {
        Specification<CourseEntity> spec = Specification.where(null);

        if (StringUtils.hasText(searchTerm)) {
            spec = spec.and(search(searchTerm));
        }

        if (status != null) {
            spec = spec.and(hasStatus(status));
        }

        return spec;
    }
}