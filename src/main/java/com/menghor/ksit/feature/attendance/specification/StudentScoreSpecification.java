package com.menghor.ksit.feature.attendance.specification;

import com.menghor.ksit.feature.attendance.models.StudentScoreEntity;
import org.springframework.data.jpa.domain.Specification;

public class StudentScoreSpecification {

    public static Specification<StudentScoreEntity> hasScoreSessionId(Long sessionId) {
        return (root, query, criteriaBuilder) ->
                sessionId != null ? criteriaBuilder.equal(root.get("scoreSession").get("id"), sessionId) : null;
    }

    public static Specification<StudentScoreEntity> hasStudentId(Long studentId) {
        return (root, query, criteriaBuilder) ->
                studentId != null ? criteriaBuilder.equal(root.get("student").get("id"), studentId) : null;
    }

    public static Specification<StudentScoreEntity> isNotDeleted() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("isDeleted"), false);
    }
}