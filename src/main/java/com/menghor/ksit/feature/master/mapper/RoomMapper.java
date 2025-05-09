package com.menghor.ksit.feature.master.mapper;

import com.menghor.ksit.feature.master.dto.request.RoomRequestDto;
import com.menghor.ksit.feature.master.dto.response.RoomResponseDto;
import com.menghor.ksit.feature.master.dto.update.RoomUpdateDto;
import com.menghor.ksit.feature.master.model.RoomEntity;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface RoomMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    RoomEntity toEntity(RoomRequestDto requestDto);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "status", source = "status")
    RoomResponseDto toResponseDto(RoomEntity roomEntity);

    // New method for updating an existing entity with non-null values from DTO
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(RoomUpdateDto dto, @MappingTarget RoomEntity entity);

    // Convert a list of RoomEntity to list of RoomResponseDto
    default List<RoomResponseDto> toResponseDtoList(List<RoomEntity> entities) {
        return entities.stream().map(this::toResponseDto).collect(Collectors.toList());
    }

    // Modified version for pagination
    default CustomPaginationResponseDto<RoomResponseDto> toRoomAllResponseDto(Page<RoomEntity> roomPage) {
        List<RoomResponseDto> content = toResponseDtoList(roomPage.getContent());

        return new CustomPaginationResponseDto<>(
                content,
                roomPage.getNumber() + 1,
                roomPage.getSize(),
                roomPage.getTotalElements(),
                roomPage.getTotalPages(),
                roomPage.isLast()
        );
    }
}
