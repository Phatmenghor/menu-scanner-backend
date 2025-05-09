package com.menghor.ksit.feature.master.specification;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.master.model.ClassEntity;
import com.menghor.ksit.feature.master.model.MajorEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

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


    // Combined search across code field
    public static Specification<ClassEntity> search(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(searchTerm)) return criteriaBuilder.conjunction();

            String term = "%" + searchTerm.toLowerCase() + "%";
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), term);
        };
    }
    
    // Specification for combining all filters
    public static Specification<ClassEntity> combine(String searchTerm, Integer academyYear, Status status,Long majorId) {
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
}