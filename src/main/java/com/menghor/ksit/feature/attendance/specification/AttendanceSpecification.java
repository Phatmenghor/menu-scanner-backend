package com.menghor.ksit.feature.attendance.specification;

import com.menghor.ksit.enumations.AttendanceStatus;
import com.menghor.ksit.feature.attendance.models.AttendanceEntity;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class AttendanceSpecification {

    public static Specification<AttendanceEntity> hasStudentId(Long studentId) {
        return (root, query, criteriaBuilder) -> {
            if (studentId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("student").get("id"), studentId);
        };
    }

    public static Specification<AttendanceEntity> hasSessionId(Long sessionId) {
        return (root, query, criteriaBuilder) -> {
            if (sessionId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("attendanceSession").get("id"), sessionId);
        };
    }

    public static Specification<AttendanceEntity> hasStatus(AttendanceStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<AttendanceEntity> recordedBetween(LocalDateTime start, LocalDateTime end) {
        return (root, query, criteriaBuilder) -> {
            if (start == null && end == null) {
                return criteriaBuilder.conjunction();
            }
            if (start == null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("recordedTime"), end);
            }
            if (end == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("recordedTime"), start);
            }
            return criteriaBuilder.between(root.get("recordedTime"), start, end);
        };
    }
}