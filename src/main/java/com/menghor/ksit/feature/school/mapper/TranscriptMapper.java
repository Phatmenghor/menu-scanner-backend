package com.menghor.ksit.feature.school.mapper;

import com.menghor.ksit.feature.attendance.models.StudentScoreEntity;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.school.dto.response.TranscriptCourseDto;
import com.menghor.ksit.feature.school.dto.response.TranscriptResponseDto;
import com.menghor.ksit.feature.school.model.ScheduleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface TranscriptMapper {

    @Mapping(target = "studentId", source = "id")
    @Mapping(target = "studentName", source = ".", qualifiedByName = "mapStudentName")
    @Mapping(target = "studentCode", source = "identifyNumber")
    @Mapping(target = "className", source = "classes.code")
    @Mapping(target = "majorName", source = ".", qualifiedByName = "mapMajorName")
    @Mapping(target = "departmentName", source = ".", qualifiedByName = "mapDepartmentName")
    @Mapping(target = "totalCreditsEarned", ignore = true)
    @Mapping(target = "totalCreditsAttempted", ignore = true)
    @Mapping(target = "overallGPA", ignore = true)
    @Mapping(target = "academicStatus", ignore = true)
    @Mapping(target = "semesters", ignore = true)
    @Mapping(target = "generatedAt", ignore = true)
    TranscriptResponseDto toTranscriptResponse(UserEntity student);

    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "courseCode", source = "course.code")
    @Mapping(target = "courseName", source = "course.nameEn")
    @Mapping(target = "courseNameKH", source = "course.nameKH")
    @Mapping(target = "credits", source = "course.credit")
    @Mapping(target = "credit", source = "course.credit")
    @Mapping(target = "theory", source = "course.theory")
    @Mapping(target = "execute", source = "course.execute")
    @Mapping(target = "apply", source = "course.apply")
    @Mapping(target = "totalHour", source = "course.totalHour")
    @Mapping(target = "scheduleId", source = "id")
    @Mapping(target = "dayOfWeek", source = "day")
    @Mapping(target = "timeSlot", source = ".", qualifiedByName = "mapTimeSlot")
    @Mapping(target = "roomName", source = "room.name")
    @Mapping(target = "teacherName", source = "user", qualifiedByName = "mapTeacherName")
    @Mapping(target = "totalScore", ignore = true)
    @Mapping(target = "letterGrade", ignore = true)
    @Mapping(target = "gradePoints", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "attendanceScore", ignore = true)
    @Mapping(target = "assignmentScore", ignore = true)
    @Mapping(target = "midtermScore", ignore = true)
    @Mapping(target = "finalScore", ignore = true)
    TranscriptCourseDto toCourseDto(ScheduleEntity schedule);

    @Mapping(target = "totalScore", source = "totalScore")
    @Mapping(target = "letterGrade", source = "grade")
    @Mapping(target = "attendanceScore", source = "attendanceScore")
    @Mapping(target = "assignmentScore", source = "assignmentScore")
    @Mapping(target = "midtermScore", source = "midtermScore")
    @Mapping(target = "finalScore", source = "finalScore")
    void mapScoreToTranscript(StudentScoreEntity score, @MappingTarget TranscriptCourseDto courseDto);

    @Named("mapStudentName")
    default String mapStudentName(UserEntity student) {
        if (student.getEnglishFirstName() != null && student.getEnglishLastName() != null) {
            return student.getEnglishFirstName() + " " + student.getEnglishLastName();
        }
        if (student.getKhmerFirstName() != null && student.getKhmerLastName() != null) {
            return student.getKhmerFirstName() + " " + student.getKhmerLastName();
        }
        return student.getUsername();
    }

    @Named("mapMajorName")
    default String mapMajorName(UserEntity student) {
        if (student.getClasses() != null && student.getClasses().getMajor() != null) {
            return student.getClasses().getMajor().getName();
        }
        return null;
    }

    @Named("mapDepartmentName")
    default String mapDepartmentName(UserEntity student) {
        if (student.getClasses() != null &&
                student.getClasses().getMajor() != null &&
                student.getClasses().getMajor().getDepartment() != null) {
            return student.getClasses().getMajor().getDepartment().getName();
        }
        return null;
    }

    @Named("mapTimeSlot")
    default String mapTimeSlot(ScheduleEntity schedule) {
        if (schedule.getStartTime() != null && schedule.getEndTime() != null) {
            return schedule.getStartTime() + " - " + schedule.getEndTime();
        }
        return null;
    }

    @Named("mapTeacherName")
    default String mapTeacherName(UserEntity teacher) {
        if (teacher == null) return null;

        if (teacher.getEnglishFirstName() != null && teacher.getEnglishLastName() != null) {
            return teacher.getEnglishFirstName() + " " + teacher.getEnglishLastName();
        }
        if (teacher.getKhmerFirstName() != null && teacher.getKhmerLastName() != null) {
            return teacher.getKhmerFirstName() + " " + teacher.getKhmerLastName();
        }
        return teacher.getUsername();
    }
}
