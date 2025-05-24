package com.menghor.ksit.feature.attendance.mapper;

import com.menghor.ksit.enumations.AttendanceStatus;
import com.menghor.ksit.feature.attendance.dto.response.AttendanceDto;
import com.menghor.ksit.feature.attendance.dto.response.AttendanceSessionDto;
import com.menghor.ksit.feature.attendance.models.AttendanceEntity;
import com.menghor.ksit.feature.attendance.models.AttendanceSessionEntity;
import com.menghor.ksit.feature.auth.models.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AttendanceMapper {
    AttendanceMapper INSTANCE = Mappers.getMapper(AttendanceMapper.class);

    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "student", target = "studentName", qualifiedByName = "mapStudentName")
    @Mapping(source = "student.identifyNumber", target = "identifyNumber")
    @Mapping(source = "attendanceSession.id", target = "attendanceSessionId")
    AttendanceDto toDto(AttendanceEntity entity);

    @Mapping(source = "schedule.id", target = "scheduleId")
    @Mapping(source = "schedule.room.name", target = "roomName")
    @Mapping(source = "schedule.classes.code", target = "classCode")
    @Mapping(source = "teacher.id", target = "teacherId")
    @Mapping(source = "teacher", target = "teacherName", qualifiedByName = "mapTeacherName")
    @Mapping(source = "finalizationStatus", target = "finalizationStatus")
    @Mapping(target = "totalStudents", expression = "java(calculateTotalStudents(entity))")
    @Mapping(target = "totalPresent", expression = "java(calculateTotalPresent(entity))")
    @Mapping(target = "totalAbsent", expression = "java(calculateTotalAbsent(entity))")
    AttendanceSessionDto toDto(AttendanceSessionEntity entity);

    // Custom method to handle the student name logic
    @Named("mapStudentName")
    default String mapStudentName(UserEntity student) {
        if (student == null) {
            return null;
        }

        // Check if English names are available and not null
        if (student.getEnglishFirstName() != null && student.getEnglishLastName() != null) {
            return student.getEnglishFirstName() + " " + student.getEnglishLastName();
        }

        // Fallback to Khmer names
        if (student.getKhmerFirstName() != null && student.getKhmerLastName() != null) {
            return student.getKhmerFirstName() + " " + student.getKhmerLastName();
        }

        // If all names are null, return username or null
        return student.getUsername();
    }

    // Custom method to handle the teacher name logic
    @Named("mapTeacherName")
    default String mapTeacherName(UserEntity teacher) {
        if (teacher == null) {
            return null;
        }

        // Check if English names are available and not null
        if (teacher.getEnglishFirstName() != null && teacher.getEnglishLastName() != null) {
            return teacher.getEnglishFirstName() + " " + teacher.getEnglishLastName();
        }

        // Fallback to Khmer names
        if (teacher.getKhmerFirstName() != null && teacher.getKhmerLastName() != null) {
            return teacher.getKhmerFirstName() + " " + teacher.getKhmerLastName();
        }

        // If all names are null, return username or null
        return teacher.getUsername();
    }

    // Count calculation methods
    default Long calculateTotalStudents(AttendanceSessionEntity entity) {
        if (entity == null || entity.getAttendances() == null) {
            return 0L;
        }
        return (long) entity.getAttendances().size();
    }

    default Long calculateTotalPresent(AttendanceSessionEntity entity) {
        if (entity == null || entity.getAttendances() == null) {
            return 0L;
        }
        return entity.getAttendances().stream()
                .filter(attendance -> attendance.getStatus() == AttendanceStatus.PRESENT)
                .count();
    }

    default Long calculateTotalAbsent(AttendanceSessionEntity entity) {
        if (entity == null || entity.getAttendances() == null) {
            return 0L;
        }
        return entity.getAttendances().stream()
                .filter(attendance -> attendance.getStatus() == AttendanceStatus.ABSENT)
                .count();
    }
}