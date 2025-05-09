package com.menghor.ksit.feature.master.service;

import com.menghor.ksit.feature.master.dto.filter.RoomFilterDto;
import com.menghor.ksit.feature.master.dto.request.RoomRequestDto;
import com.menghor.ksit.feature.master.dto.response.RoomResponseDto;
import com.menghor.ksit.feature.master.dto.update.RoomUpdateDto;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;

public interface RoomService {

    RoomResponseDto createRoom(RoomRequestDto roomRequest);

    RoomResponseDto getRoomById(Long id);

    RoomResponseDto updateRoomById(RoomUpdateDto roomRequest, Long id);

    RoomResponseDto deleteRoomById(Long id);

    CustomPaginationResponseDto<RoomResponseDto> getAllRoom(RoomFilterDto filterDto);
}
