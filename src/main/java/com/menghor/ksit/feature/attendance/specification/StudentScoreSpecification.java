package com.menghor.ksit.feature.attendance.specification;

import com.menghor.ksit.feature.attendance.models.StudentScoreEntity;
import org.springframework.data.jpa.domain.Specification;

public class StudentScoreSpecification {

    public static Specification<StudentScoreEntity> hasScoreSessionId(Long sessionId) {
        return (root, query, criteriaBuilder) -> {
            if (sessionId == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("scoreSession").get("id"), sessionId);
        };
    }

    public static Specification<StudentScoreEntity> hasStudentId(Long studentId) {
        return (root, query, criteriaBuilder) -> {
            if (studentId == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("student").get("id"), studentId);
        };
    }

    public static Specification<StudentScoreEntity> isNotDeleted() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.isNull(root.get("deletedAt"));
    }

    public static Specification<StudentScoreEntity> hasMinTotalScore(Double minScore) {
        return (root, query, criteriaBuilder) -> {
            if (minScore == null) return criteriaBuilder.conjunction();
            // This would require a complex calculation or database function
            return criteriaBuilder.conjunction(); // Simplified for now
        };
    }

    public static Specification<StudentScoreEntity> hasConfiguration(Long configId) {
        return (root, query, criteriaBuilder) -> {
            if (configId == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("scoreConfiguration").get("id"), configId);
        };
    }
}