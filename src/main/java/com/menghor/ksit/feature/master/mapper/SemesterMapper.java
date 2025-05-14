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
    @Mapping(target = "semesterType", ignore = true) // We'll set this in the afterMapping method
    SemesterResponseDto toResponseDto(SemesterEntity entity);

    // After mapping method to calculate and set the semesterType based on date comparison
    @AfterMapping
    default void setSemesterType(@MappingTarget SemesterResponseDto responseDto, SemesterEntity entity) {
        // Get current date
        LocalDate currentDate = LocalDate.now();

        // Set semesterType based on date comparison
        if (currentDate.isBefore(entity.getStartDate())) {
            responseDto.setSemesterType(SemesterType.PROGRESS);
        } else if (currentDate.isAfter(entity.getEndDate())) {
            responseDto.setSemesterType(SemesterType.DONE);
        } else {
            // Current date is between start and end dates
            responseDto.setSemesterType(SemesterType.PROCESSING);
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