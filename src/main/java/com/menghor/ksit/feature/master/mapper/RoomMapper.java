package com.menghor.ksit.feature.master.mapper;

import com.menghor.ksit.feature.master.dto.room.request.RoomRequestDto;
import com.menghor.ksit.feature.master.dto.room.response.RoomResponseDto;
import com.menghor.ksit.feature.master.model.RoomEntity;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    RoomEntity toEntity (RoomRequestDto roomRequestDto);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "status", source = "status")
    RoomResponseDto toResponseDto (RoomEntity roomEntity);

    // Convert a list of RoomEntity to list of RoomResponseDto
    default List<RoomResponseDto> toResponseDtoList(List<RoomEntity> entities) {
        return entities.stream().map(this::toResponseDto).collect(Collectors.toList());
    }

    // Modified version of your code
    default CustomPaginationResponseDto<RoomResponseDto> toRoomAllResponseDto(Page<RoomEntity> roomPage) {
        List<RoomResponseDto> content = toResponseDtoList(roomPage.getContent());

        // Fix #1: Use a constructor instead of builder
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
