package com.menghor.ksit.feature.attendance.specification;

import com.menghor.ksit.enumations.AttendanceFinalizationStatus;
import com.menghor.ksit.enumations.AttendanceStatus;
import com.menghor.ksit.enumations.SemesterEnum;
import com.menghor.ksit.feature.attendance.dto.request.AttendanceHistoryFilterDto;
import com.menghor.ksit.feature.attendance.models.AttendanceEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

public class AttendanceSpecification {

    /**
     * Search by student name, teacher name, course name, class code, room name, etc.
     */
    public static Specification<AttendanceEntity> searchWithAttendance(String search) {
        return (root, query, criteriaBuilder) -> {
            if (StringUtils.hasText(search)) {
                String searchPattern = "%" + search.toLowerCase() + "%";

                return criteriaBuilder.or(
                        // Student fields
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("student").get("identifyNumber")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("student").get("username")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("student").get("englishFirstName")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("student").get("englishLastName")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("student").get("khmerFirstName")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("student").get("khmerLastName")), searchPattern),

                        // Teacher fields (through attendance session)
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("attendanceSession").get("teacher").get("englishFirstName")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("attendanceSession").get("teacher").get("englishLastName")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("attendanceSession").get("teacher").get("khmerFirstName")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("attendanceSession").get("teacher").get("khmerLastName")), searchPattern),

                        // Course fields (through schedule)
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("attendanceSession").get("schedule").get("course").get("nameEn")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("attendanceSession").get("schedule").get("course").get("nameKH")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("attendanceSession").get("schedule").get("course").get("code")), searchPattern),

                        // Class fields
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("attendanceSession").get("schedule").get("classes").get("code")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("attendanceSession").get("schedule").get("classes").get("nameEn")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("attendanceSession").get("schedule").get("classes").get("nameKH")), searchPattern),

                        // Room fields
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("attendanceSession").get("schedule").get("room").get("name")), searchPattern)
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
     * Filter by semester type (SEMESTER_1, SEMESTER_2)
     */
    public static Specification<AttendanceEntity> hasSemester(SemesterEnum semester) {
        return (root, query, criteriaBuilder) -> {
            if (semester != null) {
                Join<Object, Object> sessionJoin = root.join("attendanceSession", JoinType.INNER);
                Join<Object, Object> scheduleJoin = sessionJoin.join("schedule", JoinType.INNER);
                Join<Object, Object> semesterJoin = scheduleJoin.join("semester", JoinType.INNER);
                return criteriaBuilder.equal(semesterJoin.get("semester"), semester);
            }
            return null;
        };
    }

    /**
     * Filter by academy year
     */
    public static Specification<AttendanceEntity> hasAcademyYear(Integer academyYear) {
        return (root, query, criteriaBuilder) -> {
            if (academyYear != null) {
                Join<Object, Object> sessionJoin = root.join("attendanceSession", JoinType.INNER);
                Join<Object, Object> scheduleJoin = sessionJoin.join("schedule", JoinType.INNER);
                Join<Object, Object> semesterJoin = scheduleJoin.join("semester", JoinType.INNER);
                return criteriaBuilder.equal(semesterJoin.get("academyYear"), academyYear);
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
     * Combine specifications using AttendanceHistoryFilterDto
     */
    public static Specification<AttendanceEntity> combine(AttendanceHistoryFilterDto filterDto) {
        Specification<AttendanceEntity> result = Specification.where(null);

        if (StringUtils.hasText(filterDto.getSearch())) {
            result = result.and(searchWithAttendance(filterDto.getSearch()));
        }

        if (filterDto.getStudentId() != null) {
            result = result.and(hasStudentId(filterDto.getStudentId()));
        }

        if (filterDto.getStatus() != null) {
            result = result.and(hasStatus(filterDto.getStatus()));
        }

        if (filterDto.getFinalizationStatus() != null) {
            result = result.and(hasFinalizationStatus(filterDto.getFinalizationStatus()));
        }

        if (filterDto.getScheduleId() != null) {
            result = result.and(hasScheduleId(filterDto.getScheduleId()));
        }

        if (filterDto.getClassId() != null) {
            result = result.and(hasClassId(filterDto.getClassId()));
        }

        if (filterDto.getTeacherId() != null) {
            result = result.and(hasTeacherId(filterDto.getTeacherId()));
        }

        if (filterDto.getSemester() != null) {
            result = result.and(hasSemester(filterDto.getSemester()));
        }

        if (filterDto.getAcademyYear() != null) {
            result = result.and(hasAcademyYear(filterDto.getAcademyYear()));
        }

        if (filterDto.getStartDate() != null || filterDto.getEndDate() != null) {
            result = result.and(recordedBetween(filterDto.getStartDate(), filterDto.getEndDate()));
        }

        return result;
    }

    /**
     * Legacy combine method for backward compatibility
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