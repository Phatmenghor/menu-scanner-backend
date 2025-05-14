package com.menghor.ksit.feature.master.specification;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.master.model.SemesterEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class SemesterSpecification {

    public static Specification<SemesterEntity> hasName(String name) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(name)) return criteriaBuilder.conjunction();
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<SemesterEntity> hasAcademyYear(Integer academyYear) {
        return (root, query, criteriaBuilder) -> {
            if (academyYear == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("academyYear"), academyYear);
        };
    }

    public static Specification<SemesterEntity> hasStatus(Status status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    // Combined search across academyYear and semester fields
    public static Specification<SemesterEntity> search(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(searchTerm)) return criteriaBuilder.conjunction();

            String term = "%" + searchTerm.toLowerCase() + "%";

            return criteriaBuilder.or(
                    // Search by academyYear (converted to string for LIKE comparison)
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("academyYear").as(String.class)),
                            term
                    ),
                    // Search by semester enum value (string representation)
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("semester").as(String.class)),
                            term
                    )
            );
        };
    }
    
    // Specification for all semesters
    public static Specification<SemesterEntity> combine(String searchTerm, Integer academyYear, Status status) {
        Specification<SemesterEntity> spec = Specification.where(null);

        if (StringUtils.hasText(searchTerm)) {
            spec = spec.and(search(searchTerm));
        }

        if (academyYear != null) {
            spec = spec.and(hasAcademyYear(academyYear));
        }

        if (status != null) {
            spec = spec.and(hasStatus(status));
        }

        return spec;
    }
}