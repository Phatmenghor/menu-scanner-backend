package com.menghor.ksit.feature.master.mapper;

import com.menghor.ksit.feature.master.dto.request.SemesterRequestDto;
import com.menghor.ksit.feature.master.dto.response.SemesterResponseDto;
import com.menghor.ksit.feature.master.dto.update.SemesterUpdateDto;
import com.menghor.ksit.feature.master.model.SemesterEntity;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import com.menghor.ksit.enumations.SemesterType;
import com.menghor.ksit.enumations.Status;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface SemesterMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SemesterEntity toEntity(SemesterRequestDto requestDto);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "endDate", source = "endDate")
    @Mapping(target = "academyYear", source = "academyYear")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "semester", source = "semester")
    @Mapping(target = "semesterType", source = "entity", qualifiedByName = "calculateSemesterType")
    SemesterResponseDto toResponseDto(SemesterEntity entity);

    // Named method to calculate semester type
    @Named("calculateSemesterType")
    default SemesterType calculateSemesterType(SemesterEntity entity) {
        if (entity == null) {
            return SemesterType.PROGRESS;
        }

        LocalDate currentDate = LocalDate.now();
        LocalDate startDate = entity.getStartDate();
        LocalDate endDate = entity.getEndDate();

        // Check if dates are null
        if (startDate == null || endDate == null) {
            return SemesterType.PROGRESS;
        }

        // Set semesterType based on date comparison
        if (currentDate.isBefore(startDate)) {
            // Semester hasn't started yet
            return SemesterType.PROGRESS;
        } else if (currentDate.isAfter(endDate)) {
            // Semester has ended
            return SemesterType.DONE;
        } else {
            // Current date is between start and end dates - semester is currently active
            return SemesterType.PROCESSING;
        }
    }

    // Method for updating an existing entity with non-null values from DTO
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(SemesterUpdateDto dto, @MappingTarget SemesterEntity entity);

    default List<SemesterResponseDto> toResponseDtoList(List<SemesterEntity> entities) {
        return entities.stream().map(this::toResponseDto).collect(Collectors.toList());
    }

    default CustomPaginationResponseDto<SemesterResponseDto> toSemesterAllResponseDto(Page<SemesterEntity> semesterPage) {
        List<SemesterResponseDto> content = toResponseDtoList(semesterPage.getContent());

        return new CustomPaginationResponseDto<>(
                content,
                semesterPage.getNumber() + 1,
                semesterPage.getSize(),
                semesterPage.getTotalElements(),
                semesterPage.getTotalPages(),
                semesterPage.isLast()
        );
    }
}