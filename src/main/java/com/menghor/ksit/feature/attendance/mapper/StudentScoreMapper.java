package com.menghor.ksit.feature.attendance.mapper;

import com.menghor.ksit.feature.attendance.dto.response.StudentScoreResponseDto;
import com.menghor.ksit.feature.attendance.models.StudentScoreEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Component
public class StudentScoreMapper {

    public StudentScoreResponseDto toDto(StudentScoreEntity entity) {
        if (entity == null) {
            return null;
        }

        StudentScoreResponseDto dto = new StudentScoreResponseDto();

        // Basic score entity fields
        dto.setId(entity.getId());
        dto.setAttendanceRawScore(entity.getAttendanceRawScore());
        dto.setAssignmentRawScore(entity.getAssignmentRawScore());
        dto.setMidtermRawScore(entity.getMidtermRawScore());
        dto.setFinalRawScore(entity.getFinalRawScore());
        dto.setAttendanceScore(entity.getAttendanceScore());
        dto.setAssignmentScore(entity.getAssignmentScore());
        dto.setMidtermScore(entity.getMidtermScore());
        dto.setFinalScore(entity.getFinalScore());
        dto.setTotalScore(entity.getTotalScore());
        dto.setGrade(entity.getGrade());
        dto.setComments(entity.getComments());
        dto.setCreatedAt(entity.getCreatedAt());

        // Student fields - update these based on your actual UserEntity fields
        if (entity.getStudent() != null) {
            dto.setStudentId(entity.getStudent().getId());

            // TODO: Update these with your actual UserEntity field names:
            // dto.setStudentNameKhmer(entity.getStudent().getKhmerName());
            // dto.setStudentNameEnglish(entity.getStudent().getEnglishName());
            // dto.setStudentIdentityNumber(entity.getStudent().getIdentityNumber());
            // dto.setGender(entity.getStudent().getGender());
            // dto.setDateOfBirth(entity.getStudent().getDateOfBirth());

            // Temporary fallback values to avoid errors
            dto.setStudentNameKhmer("Student Khmer Name");
            dto.setStudentNameEnglish("Student English Name");
            dto.setStudentIdentityNumber(entity.getStudent().getId()); // Using ID as fallback
            dto.setGender("N/A");
            dto.setDateOfBirth(null);
        }

        return dto;
    }
}