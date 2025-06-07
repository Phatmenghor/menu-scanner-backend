package com.menghor.ksit.feature.attendance.mapper;

import com.menghor.ksit.feature.attendance.dto.response.ScoreSessionResponseDto;
import com.menghor.ksit.feature.attendance.models.ScoreSessionEntity;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.master.model.ClassEntity;
import com.menghor.ksit.feature.school.model.CourseEntity;
import com.menghor.ksit.feature.school.model.ScheduleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {StudentScoreMapper.class})
public interface ScoreSessionMapper {

    ScoreSessionMapper INSTANCE = Mappers.getMapper(ScoreSessionMapper.class);

    @Mapping(source = "schedule.id", target = "scheduleId")
    @Mapping(source = "teacher.id", target = "teacherId")
    @Mapping(source = "teacher", target = "teacherName", qualifiedByName = "mapTeacherName")
    @Mapping(source = "schedule.classes.id", target = "classId")
    @Mapping(source = "schedule.classes", target = "classCode", qualifiedByName = "mapClassCode")
    @Mapping(source = "schedule.course.id", target = "courseId")
    @Mapping(source = "schedule.semester.semester", target = "semester")
    @Mapping(source = "schedule.course", target = "courseName", qualifiedByName = "mapCourseName")
    @Mapping(source = "studentScores", target = "studentScores")
    ScoreSessionResponseDto toDto(ScoreSessionEntity entity);

    @Named("mapTeacherName")
    default String mapTeacherName(UserEntity teacher) {
        if (teacher == null) {
            return null;
        }

        if (teacher.getEnglishFirstName() != null && teacher.getEnglishLastName() != null) {
            return teacher.getEnglishFirstName() + " " + teacher.getEnglishLastName();
        }

        if (teacher.getKhmerFirstName() != null && teacher.getKhmerLastName() != null) {
            return teacher.getKhmerFirstName() + " " + teacher.getKhmerLastName();
        }

        return teacher.getUsername();
    }

    @Named("mapClassCode")
    default String mapClassName(ClassEntity classEntity) {
        if (classEntity == null) {
            return null;
        }
        return classEntity.getCode() != null ? classEntity.getCode() : "Class #" + classEntity.getId();
    }

    @Named("mapCourseName")
    default String mapCourseName(CourseEntity course) {
        if (course == null) {
            return null;
        }
        return course.getNameEn() != null ? course.getNameEn() :
                course.getNameKH() != null ? course.getNameKH() : "Course #" + course.getId();
    }
}