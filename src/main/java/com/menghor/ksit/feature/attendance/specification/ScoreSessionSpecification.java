package com.menghor.ksit.feature.attendance.specification;

import com.menghor.ksit.enumations.SubmissionStatus;
import com.menghor.ksit.feature.attendance.models.ScoreSessionEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.JoinType;

public class ScoreSessionSpecification {

    /**
     * Search by course name or class code
     */
    public static Specification<ScoreSessionEntity> searchByNameOrCode(String search) {
        return (root, query, criteriaBuilder) -> {
            if (StringUtils.hasText(search)) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                return criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("schedule").get("course").get("nameEn")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("schedule").get("course").get("nameKh")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("schedule").get("classes").get("code")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("schedule").get("classes").get("name")), searchPattern)
                );
            }
            return null;
        };
    }

    /**
     * Filter by submission status
     */
    public static Specification<ScoreSessionEntity> hasStatus(SubmissionStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status != null) {
                return criteriaBuilder.equal(root.get("status"), status);
            }
            return null;
        };
    }

    /**
     * Filter by teacher ID
     */
    public static Specification<ScoreSessionEntity> hasTeacherId(Long teacherId) {
        return (root, query, criteriaBuilder) -> {
            if (teacherId != null) {
                return criteriaBuilder.equal(root.get("teacher").get("id"), teacherId);
            }
            return null;
        };
    }

    /**
     * Filter by schedule ID
     */
    public static Specification<ScoreSessionEntity> hasScheduleId(Long scheduleId) {
        return (root, query, criteriaBuilder) -> {
            if (scheduleId != null) {
                return criteriaBuilder.equal(root.get("schedule").get("id"), scheduleId);
            }
            return null;
        };
    }

    /**
     * Filter by class ID
     */
    public static Specification<ScoreSessionEntity> hasClassId(Long classId) {
        return (root, query, criteriaBuilder) -> {
            if (classId != null) {
                return criteriaBuilder.equal(root.get("schedule").get("classes").get("id"), classId);
            }
            return null;
        };
    }

    /**
     * Filter by course ID
     */
    public static Specification<ScoreSessionEntity> hasCourseId(Long courseId) {
        return (root, query, criteriaBuilder) -> {
            if (courseId != null) {
                return criteriaBuilder.equal(root.get("schedule").get("course").get("id"), courseId);
            }
            return null;
        };
    }

    /**
     * Filter by student ID - NEW METHOD
     * This filters score sessions that have student scores for the specified student
     */
    public static Specification<ScoreSessionEntity> hasStudentId(Long studentId) {
        return (root, query, criteriaBuilder) -> {
            if (studentId != null) {
                // Join with studentScores and filter by student ID
                var studentScoresJoin = root.join("studentScores", JoinType.INNER);
                return criteriaBuilder.equal(studentScoresJoin.get("student").get("id"), studentId);
            }
            return null;
        };
    }

    /**
     * Combine multiple specifications with AND operator
     * Updated to include studentId parameter
     */
    public static Specification<ScoreSessionEntity> combine(String search, SubmissionStatus status,
                                                            Long teacherId, Long scheduleId,
                                                            Long classId, Long courseId, Long studentId) {

        Specification<ScoreSessionEntity> result = Specification.where(null);

        if (StringUtils.hasText(search)) {
            result = result.and(searchByNameOrCode(search));
        }

        if (status != null) {
            result = result.and(hasStatus(status));
        }

        if (teacherId != null) {
            result = result.and(hasTeacherId(teacherId));
        }

        if (scheduleId != null) {
            result = result.and(hasScheduleId(scheduleId));
        }

        if (classId != null) {
            result = result.and(hasClassId(classId));
        }

        if (courseId != null) {
            result = result.and(hasCourseId(courseId));
        }

        if (studentId != null) {
            result = result.and(hasStudentId(studentId));
        }

        return result;
    }
}