package com.menghor.ksit.feature.attendance.mapper;

import com.menghor.ksit.feature.attendance.dto.request.ScoreConfigurationRequestDto;
import com.menghor.ksit.feature.attendance.dto.response.ScoreConfigurationResponseDto;
import com.menghor.ksit.feature.attendance.models.ScoreConfigurationEntity;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ScoreConfigurationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", expression = "java(com.menghor.ksit.enumations.Status.ACTIVE)")
    ScoreConfigurationEntity toEntity(ScoreConfigurationRequestDto requestDto);

    @Mapping(target = "totalPercentage", expression = "java(entity.getTotalPercentage())")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "formatDateTime")
    ScoreConfigurationResponseDto toResponseDto(ScoreConfigurationEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateEntityFromDto(ScoreConfigurationRequestDto requestDto, @MappingTarget ScoreConfigurationEntity entity);

    default ScoreConfigurationEntity createDefaultConfiguration() {
        ScoreConfigurationEntity entity = new ScoreConfigurationEntity();
        entity.setAttendancePercentage(10);
        entity.setAssignmentPercentage(20);
        entity.setMidtermPercentage(30);
        entity.setFinalPercentage(40);
        entity.setStatus(com.menghor.ksit.enumations.Status.ACTIVE);
        return entity;
    }

    default ScoreConfigurationEntity createConfiguration(
            Integer attendancePercentage,
            Integer assignmentPercentage,
            Integer midtermPercentage,
            Integer finalPercentage) {

        ScoreConfigurationEntity entity = new ScoreConfigurationEntity();
        entity.setAttendancePercentage(attendancePercentage);
        entity.setAssignmentPercentage(assignmentPercentage);
        entity.setMidtermPercentage(midtermPercentage);
        entity.setFinalPercentage(finalPercentage);
        entity.setStatus(com.menghor.ksit.enumations.Status.ACTIVE);
        return entity;
    }

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