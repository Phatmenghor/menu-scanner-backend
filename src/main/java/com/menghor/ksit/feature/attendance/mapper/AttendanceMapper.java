package com.menghor.ksit.feature.attendance.mapper;

import com.menghor.ksit.enumations.AttendanceStatus;
import com.menghor.ksit.feature.attendance.dto.response.AttendanceDto;
import com.menghor.ksit.feature.attendance.dto.response.AttendanceSessionDto;
import com.menghor.ksit.feature.attendance.models.AttendanceEntity;
import com.menghor.ksit.feature.attendance.models.AttendanceSessionEntity;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.master.model.ClassEntity;
import com.menghor.ksit.feature.master.model.RoomEntity;
import com.menghor.ksit.feature.master.model.SemesterEntity;
import com.menghor.ksit.feature.school.model.CourseEntity;
import com.menghor.ksit.feature.school.model.ScheduleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AttendanceMapper {
    AttendanceMapper INSTANCE = Mappers.getMapper(AttendanceMapper.class);

    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "student", target = "studentName", qualifiedByName = "mapStudentName")
    @Mapping(source = "student.identifyNumber", target = "identifyNumber")
    @Mapping(source = "student.gender", target = "gender")
    @Mapping(source = "student.dateOfBirth", target = "dateOfBirth")
    @Mapping(source = "attendanceSession.id", target = "attendanceSessionId")
    @Mapping(source = "attendanceSession.teacher.id", target = "teacherId")
    @Mapping(source = "attendanceSession.teacher", target = "teacherName", qualifiedByName = "mapTeacherName")
    @Mapping(source = "attendanceSession.schedule.id", target = "scheduleId")

    // Enhanced Course Mapping
    @Mapping(source = "attendanceSession.schedule.course", target = "courseName", qualifiedByName = "mapCourseName")
    @Mapping(source = "attendanceSession.schedule.course.nameKH", target = "courseNameKH")
    @Mapping(source = "attendanceSession.schedule.course.nameEn", target = "courseNameEn")
    @Mapping(source = "attendanceSession.schedule.course.code", target = "courseCode")
    @Mapping(source = "attendanceSession.schedule.course.credit", target = "credit")
    @Mapping(source = "attendanceSession.schedule.course.theory", target = "theory")
    @Mapping(source = "attendanceSession.schedule.course.execute", target = "execute")
    @Mapping(source = "attendanceSession.schedule.course.apply", target = "apply")
    @Mapping(source = "attendanceSession.schedule.course.totalHour", target = "totalHour")

    // Enhanced Schedule Mapping
    @Mapping(source = "attendanceSession.schedule.startTime", target = "startTime")
    @Mapping(source = "attendanceSession.schedule.endTime", target = "endTime")
    @Mapping(source = "attendanceSession.schedule.day", target = "day")
    @Mapping(source = "attendanceSession.schedule.yearLevel", target = "yearLevel")

    @Mapping(source = "attendanceSession.schedule.course.department.urlLogo", target = "departmentImageUrl")

    // Room Information
    @Mapping(source = "attendanceSession.schedule.room.id", target = "roomId")
    @Mapping(source = "attendanceSession.schedule.room.name", target = "roomName")

    // Class Information
    @Mapping(source = "attendanceSession.schedule.classes.id", target = "classId")
    @Mapping(source = "attendanceSession.schedule.classes.code", target = "classCode")

    // Semester Information
    @Mapping(source = "attendanceSession.schedule.semester.id", target = "semesterId")
    @Mapping(source = "attendanceSession.schedule.semester.semester", target = "semester")
    @Mapping(source = "attendanceSession.schedule.semester", target = "semesterName", qualifiedByName = "mapSemesterName")
    @Mapping(source = "attendanceSession.schedule.semester.academyYear", target = "academyYear")
    AttendanceDto toDto(AttendanceEntity entity);

    @Mapping(source = "schedule.id", target = "scheduleId")
    @Mapping(source = "schedule.room.name", target = "roomName")
    @Mapping(source = "schedule.classes.code", target = "classCode")
    @Mapping(source = "teacher.id", target = "teacherId")
    @Mapping(source = "teacher", target = "teacherName", qualifiedByName = "mapTeacherName")
    @Mapping(source = "finalizationStatus", target = "finalizationStatus")
    @Mapping(source = "attendances", target = "attendances", qualifiedByName = "sortAttendances")
    @Mapping(target = "totalStudents", expression = "java(calculateTotalStudents(entity))")
    @Mapping(target = "totalPresent", expression = "java(calculateTotalPresent(entity))")
    @Mapping(target = "totalAbsent", expression = "java(calculateTotalAbsent(entity))")
    AttendanceSessionDto toDto(AttendanceSessionEntity entity);

    // Custom method to sort attendances: PRESENT first, then by createdAt
    @Named("sortAttendances")
    default List<AttendanceDto> sortAttendances(List<AttendanceEntity> attendances) {
        if (attendances == null || attendances.isEmpty()) {
            return new ArrayList<>();
        }

        return attendances.stream()
                .sorted(Comparator
                        .comparing((AttendanceEntity a) -> a.getStatus().ordinal()) // PRESENT (0) first, ABSENT (1) second
                        .thenComparing(AttendanceEntity::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))) // Then by createdAt ascending, nulls last
                .map(this::toDto)
                .collect(Collectors.toList());
    }

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
        return null;
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

    // Custom method to handle the course name logic
    @Named("mapCourseName")
    default String mapCourseName(CourseEntity course) {
        if (course == null) {
            return null;
        }

        // Prefer English name, fallback to Khmer name, then fallback to code
        if (course.getNameEn() != null && !course.getNameEn().trim().isEmpty()) {
            return course.getNameEn();
        }

        if (course.getNameKH() != null && !course.getNameKH().trim().isEmpty()) {
            return course.getNameKH();
        }

        if (course.getCode() != null && !course.getCode().trim().isEmpty()) {
            return course.getCode();
        }

        return "Course #" + course.getId();
    }

    // Custom method to handle the semester name logic
    @Named("mapSemesterName")
    default String mapSemesterName(SemesterEntity semester) {
        if (semester == null) {
            return null;
        }

        // Create a readable semester name like "Semester 1, 2024"
        String semesterName = "";
        if (semester.getSemester() != null) {
            semesterName = semester.getSemester().name().replace("_", " ");
            // Convert "SEMESTER 1" to "Semester 1"
            semesterName = semesterName.substring(0, 1).toUpperCase() +
                    semesterName.substring(1).toLowerCase();
        }

        if (semester.getAcademyYear() != null) {
            semesterName += ", " + semester.getAcademyYear();
        }

        return semesterName.isEmpty() ? "Semester #" + semester.getId() : semesterName;
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