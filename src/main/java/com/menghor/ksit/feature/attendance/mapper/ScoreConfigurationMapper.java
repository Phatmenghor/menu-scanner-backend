package com.menghor.ksit.feature.attendance.mapper;

import com.menghor.ksit.feature.attendance.dto.request.ScoreConfigurationRequestDto;
import com.menghor.ksit.feature.attendance.dto.response.ScoreConfigurationResponseDto;
import com.menghor.ksit.feature.attendance.models.ScoreConfigurationEntity;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ScoreConfigurationMapper {

    // Convert DTO to Entity (for new entities)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", expression = "java(com.menghor.ksit.enumations.Status.ACTIVE)")
    ScoreConfigurationEntity toEntity(ScoreConfigurationRequestDto requestDto);

    // Convert Entity to Response DTO
    @Mapping(target = "totalPercentage", expression = "java(entity.getTotalPercentage())")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "formatDateTime")
    ScoreConfigurationResponseDto toResponseDto(ScoreConfigurationEntity entity);

    // Update existing entity from DTO (for updates)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateEntityFromDto(ScoreConfigurationRequestDto requestDto, @MappingTarget ScoreConfigurationEntity entity);

    // Create default configuration
    default ScoreConfigurationEntity createDefaultConfiguration() {
        ScoreConfigurationEntity entity = new ScoreConfigurationEntity();
        entity.setAttendancePercentage(BigDecimal.valueOf(10.00));
        entity.setAssignmentPercentage(BigDecimal.valueOf(20.00));
        entity.setMidtermPercentage(BigDecimal.valueOf(30.00));
        entity.setFinalPercentage(BigDecimal.valueOf(40.00));
        entity.setStatus(com.menghor.ksit.enumations.Status.ACTIVE);
        return entity;
    }

    // Create entity from individual percentages (bonus utility method)
    default ScoreConfigurationEntity createConfiguration(
            BigDecimal attendancePercentage,
            BigDecimal assignmentPercentage,
            BigDecimal midtermPercentage,
            BigDecimal finalPercentage) {

        ScoreConfigurationEntity entity = new ScoreConfigurationEntity();
        entity.setAttendancePercentage(attendancePercentage);
        entity.setAssignmentPercentage(assignmentPercentage);
        entity.setMidtermPercentage(midtermPercentage);
        entity.setFinalPercentage(finalPercentage);
        entity.setStatus(com.menghor.ksit.enumations.Status.ACTIVE);
        return entity;
    }

    // Utility method to copy entity (for cloning)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ScoreConfigurationEntity copyEntity(ScoreConfigurationEntity source);

    @Named("formatDateTime")
    default String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}