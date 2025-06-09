package com.menghor.ksit.feature.attendance.specification;

import com.menghor.ksit.enumations.AttendanceFinalizationStatus;
import com.menghor.ksit.enumations.AttendanceStatus;
import com.menghor.ksit.feature.attendance.models.AttendanceEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

public class AttendanceSpecification {

    /**
     * Search by student name
     */
    public static Specification<AttendanceEntity> searchWithAttendance(String search) {
        return (root, query, criteriaBuilder) -> {
            if (StringUtils.hasText(search)) {
                String searchPattern = "%" + search.toLowerCase() + "%";

                // Add null checks for nested entities
                Join<Object, Object> sessionJoin = root.join("attendanceSession", JoinType.LEFT);
                Join<Object, Object> scheduleJoin = sessionJoin.join("schedule", JoinType.LEFT);
                Join<Object, Object> courseJoin = scheduleJoin.join("course", JoinType.LEFT);
                Join<Object, Object> classJoin = scheduleJoin.join("classes", JoinType.LEFT);
                Join<Object, Object> roomJoin = scheduleJoin.join("room", JoinType.LEFT);
                Join<Object, Object> teacherJoin = sessionJoin.join("teacher", JoinType.LEFT);

                return criteriaBuilder.or(
                        // Student fields
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("student").get("identifyNumber")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("student").get("username")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("student").get("englishFirstName")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("student").get("englishLastName")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("student").get("khmerFirstName")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("student").get("khmerLastName")), searchPattern),

                        // Course fields
                        criteriaBuilder.like(criteriaBuilder.lower(courseJoin.get("nameEn")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(courseJoin.get("nameKH")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(courseJoin.get("code")), searchPattern),

                        // Class fields
                        criteriaBuilder.like(criteriaBuilder.lower(classJoin.get("code")), searchPattern),

                        // Teacher fields
                        criteriaBuilder.like(criteriaBuilder.lower(teacherJoin.get("khmerFirstName")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(teacherJoin.get("khmerLastName")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(teacherJoin.get("username")), searchPattern),

                        // Room fields
                        criteriaBuilder.like(criteriaBuilder.lower(roomJoin.get("name")), searchPattern)
                );
            }
            return null;
        };
    }

    /**
     * Filter by student ID
     */
    public static Specification<AttendanceEntity> hasStudentId(Long studentId) {
        return (root, query, criteriaBuilder) -> {
            if (studentId != null) {
                return criteriaBuilder.equal(root.get("student").get("id"), studentId);
            }
            return null;
        };
    }

    /**
     * Filter by session ID
     */
    public static Specification<AttendanceEntity> hasSessionId(Long sessionId) {
        return (root, query, criteriaBuilder) -> {
            if (sessionId != null) {
                return criteriaBuilder.equal(root.get("attendanceSession").get("id"), sessionId);
            }
            return null;
        };
    }

    /**
     * Filter by attendance status
     */
    public static Specification<AttendanceEntity> hasStatus(AttendanceStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status != null) {
                return criteriaBuilder.equal(root.get("status"), status);
            }
            return null;
        };
    }

    /**
     * Filter by finalization status
     */
    public static Specification<AttendanceEntity> hasFinalizationStatus(AttendanceFinalizationStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status != null) {
                return criteriaBuilder.equal(root.get("finalizationStatus"), status);
            }
            return null;
        };
    }

    /**
     * Filter by schedule ID
     */
    public static Specification<AttendanceEntity> hasScheduleId(Long scheduleId) {
        return (root, query, criteriaBuilder) -> {
            if (scheduleId != null) {
                return criteriaBuilder.equal(root.get("attendanceSession").get("schedule").get("id"), scheduleId);
            }
            return null;
        };
    }

    /**
     * Filter by class ID
     */
    public static Specification<AttendanceEntity> hasClassId(Long classId) {
        return (root, query, criteriaBuilder) -> {
            if (classId != null) {
                return criteriaBuilder.equal(root.get("attendanceSession").get("schedule").get("classes").get("id"), classId);
            }
            return null;
        };
    }

    /**
     * Filter by teacher ID
     */
    public static Specification<AttendanceEntity> hasTeacherId(Long teacherId) {
        return (root, query, criteriaBuilder) -> {
            if (teacherId != null) {
                return criteriaBuilder.equal(root.get("attendanceSession").get("teacher").get("id"), teacherId);
            }
            return null;
        };
    }

    /**
     * Filter by recorded date range
     */
    public static Specification<AttendanceEntity> recordedBetween(LocalDate start, LocalDate end) {
        return (root, query, criteriaBuilder) -> {
            if (start != null && end != null) {
                return criteriaBuilder.between(root.get("recordedTime"),
                        start.atStartOfDay(),
                        end.plusDays(1).atStartOfDay().minusNanos(1));
            } else if (start != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("recordedTime"), start.atStartOfDay());
            } else if (end != null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("recordedTime"),
                        end.plusDays(1).atStartOfDay().minusNanos(1));
            }
            return null;
        };
    }

    /**
     * Combine multiple specifications with AND operator
     */
    public static Specification<AttendanceEntity> combine(String search, AttendanceStatus status,
                                                          AttendanceFinalizationStatus finalizationStatus, Long scheduleId, Long classId,
                                                          Long teacherId, LocalDate startDate, LocalDate endDate) {

        Specification<AttendanceEntity> result = Specification.where(null);

        if (StringUtils.hasText(search)) {
            result = result.and(searchWithAttendance(search));
        }

        if (status != null) {
            result = result.and(hasStatus(status));
        }

        if (finalizationStatus != null) {
            result = result.and(hasFinalizationStatus(finalizationStatus));
        }

        if (scheduleId != null) {
            result = result.and(hasScheduleId(scheduleId));
        }

        if (classId != null) {
            result = result.and(hasClassId(classId));
        }

        if (teacherId != null) {
            result = result.and(hasTeacherId(teacherId));
        }

        if (startDate != null || endDate != null) {
            result = result.and(recordedBetween(startDate, endDate));
        }

        return result;
    }
}