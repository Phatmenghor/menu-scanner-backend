package com.menghor.ksit.feature.school.specification;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.master.model.DepartmentEntity;
import com.menghor.ksit.feature.master.model.SubjectEntity;
import com.menghor.ksit.feature.school.model.CourseEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class CourseSpecification {

    /**
     * Search courses by any searchable field
     */
    public static Specification<CourseEntity> search(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(searchTerm)) return criteriaBuilder.conjunction();

            String term = "%" + searchTerm.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), term));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("nameKH")), term));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("nameEn")), term));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), term));
            
            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Filter courses by department
     */
    public static Specification<CourseEntity> hasDepartment(Long departmentId) {
        return (root, query, criteriaBuilder) -> {
            if (departmentId == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("department").get("id"), departmentId);
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
     * Combine specifications for filtering
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

    /**
     * Combine specifications with department filter
     */
    public static Specification<CourseEntity> combine(String searchTerm, Long departmentId, Status status) {
        Specification<CourseEntity> spec = combine(searchTerm, status);

        if (departmentId != null) {
            spec = spec.and(hasDepartment(departmentId));
        }

        return spec;
    }
}