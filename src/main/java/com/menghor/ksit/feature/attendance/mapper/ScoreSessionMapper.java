package com.menghor.ksit.feature.attendance.mapper;

import com.menghor.ksit.feature.attendance.dto.response.ScoreSessionResponseDto;
import com.menghor.ksit.feature.attendance.dto.response.StudentScoreResponseDto;
import com.menghor.ksit.feature.attendance.models.ScoreSessionEntity;
import com.menghor.ksit.feature.attendance.models.StudentScoreEntity;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.master.mapper.SemesterMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ScoreSessionMapper {

    private final StudentScoreMapper studentScoreMapper;

    public ScoreSessionMapper(StudentScoreMapper studentScoreMapper) {
        this.studentScoreMapper = studentScoreMapper;
    }

    public ScoreSessionResponseDto toDto(ScoreSessionEntity entity) {
        if (entity == null) {
            return null;
        }

        ScoreSessionResponseDto dto = new ScoreSessionResponseDto();

        // Basic session fields
        dto.setId(entity.getId());
        dto.setStatus(entity.getStatus());
        dto.setSubmissionDate(entity.getSubmissionDate());
        dto.setTeacherComments(entity.getTeacherComments());
        dto.setStaffComments(entity.getStaffComments());
        dto.setCreatedAt(entity.getCreatedAt());

        // Schedule fields - update these based on your actual entity field names
        if (entity.getSchedule() != null) {
            dto.setScheduleId(entity.getSchedule().getId());
            dto.setScheduleName("Schedule #" + entity.getSchedule().getId()); // Update with actual field name

            // Class fields - update based on your actual ClassEntity fields
            if (entity.getSchedule().getClasses() != null) {
                dto.setClassId(entity.getSchedule().getClasses().getId());
                dto.setClassName("Class #" + entity.getSchedule().getClasses().getId()); // Update with actual field name
            }

            // Course fields - update based on your actual CourseEntity fields
            if (entity.getSchedule().getCourse() != null) {
                dto.setCourseId(entity.getSchedule().getCourse().getId());
                dto.setCourseName("Course #" + entity.getSchedule().getCourse().getId()); // Update with actual field name
            }
        }

        // Teacher fields - update based on your actual UserEntity fields
        if (entity.getTeacher() != null) {
            dto.setTeacherId(entity.getTeacher().getId());
            dto.setTeacherName("Teacher #" + entity.getTeacher().getId()); // Update with actual field name
        }

        // Student scores
        if (entity.getStudentScores() != null) {
            dto.setStudentScores(
                    entity.getStudentScores().stream()
                            .map(studentScoreMapper::toDto)
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }
}